package quilt.internal.plugin;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildServiceRegistry;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.enigma.api.service.JarIndexerService;
import quilt.internal.constants.Constants;
import quilt.internal.extension.QuiltMappingsExtension;
import quilt.internal.plugin.abstraction.DefaultExtensionedMappingsProjectPlugin;
import quilt.internal.task.ArtifactFileTask;
import quilt.internal.task.EnigmaProfileConsumingTask;
import quilt.internal.task.MappingsDirConsumingTask;
import quilt.internal.task.QuiltMappingsArtifactTask;
import quilt.internal.task.diff.TargetVersionConsumingTask;
import quilt.internal.task.mappings.MappingsDirOutputtingTask;
import quilt.internal.util.EnigmaProfileService;
import quilt.internal.util.Version;

import java.util.Collection;
import java.util.stream.Stream;

import static org.quiltmc.enigma_plugin.Arguments.SIMPLE_TYPE_FIELD_NAMES_PATH;

/**
 * Base Quilt Mappings plugin.
 * <p>
 * Adds no tasks, but performs setup and {@linkplain TaskCollection#configureEach configures} several types of tasks:
 * <ul>
 *     <li> creates the {@value QuiltMappingsExtension#NAME} extension
 *     <li> {@linkplain TaskContainer#register registers} the
 *          {@value EnigmaProfileService#ENIGMA_PROFILE_SERVICE_NAME} service
 *     <li> {@linkplain TaskCollection#configureEach configures} the following defaults for
 *          {@link EnigmaProfileConsumingTask}s:
 *          <ul>
 *              <li> {@link EnigmaProfileConsumingTask#getEnigmaProfileService() enigmaProfileService}:
 *                   the {@value EnigmaProfileService#ENIGMA_PROFILE_SERVICE_NAME} service
 *                   which reads {@link QuiltMappingsExtension MappingsExtension}'s
 *                   {@link QuiltMappingsExtension#getEnigmaProfileConfig() enigmaProfileConfig}
 *              <li> {@link EnigmaProfileConsumingTask#getEnigmaProfileConfig() enigmaProfileConfig}:
 *                   {@link QuiltMappingsExtension MappingsExtension}'s
 *                   {@link QuiltMappingsExtension#getEnigmaProfileConfig() enigmaProfileConfig}
 *              <li> {@link EnigmaProfileConsumingTask#getProfileFileDependencies() profileFileDependencies}:
 *                   the {@value EnigmaProfileService#ENIGMA_PROFILE_SERVICE_NAME} service's
 *                   {@link EnigmaProfileService#getProfile() profile}'s
 *                   {@value org.quiltmc.enigma_plugin.Arguments#SIMPLE_TYPE_FIELD_NAMES_PATH}s
 *          </ul>
 *     <li> {@linkplain TaskCollection#configureEach configures}
 *          {@link MappingsDirOutputtingTask}s' default {@link MappingsDirOutputtingTask#getMappingsDir() mappingsDir}s
 *          to be {@link QuiltMappingsExtension}'s {@link QuiltMappingsExtension#getMappingsDir() mappingsDir}
 *     <li> {@linkplain TaskCollection#configureEach configures} the following defaults for
 *          {@link MappingsDirConsumingTask}s:
 *          <ul>
 *              <li> {@link MappingsDirConsumingTask#getMappingsDir() mappingsDir}:
 *                   {@link QuiltMappingsExtension}'s
 *                   {@link QuiltMappingsExtension#getMappingsDir() mappingsDir}
 *              <li> passes the {@link Task#getOutputs() outputs} of each {@link MappingsDirOutputtingTask}
 *                   to {@link Task#getInputs() inputs}
 *          </ul>
 *    <li> {@linkplain TaskCollection#configureEach configures} the following defaults for
 *         {@link QuiltMappingsArtifactTask}s that subclass {@link ArtifactFileTask}/{@link AbstractArchiveTask}:
 *         <ul>
 *             <li> {@link ArtifactFileTask#getArtifactBaseName() artifactBaseName}/{@link
 *                  AbstractArchiveTask#getArchiveBaseName() archiveBaseName}: {@value Constants#MAPPINGS_NAME}
 *             <li> {@link ArtifactFileTask#getArtifactVersion() artifactVersion}/{@link
 *                  AbstractArchiveTask#getArchiveVersion() archiveVersion}:
 *                  {@link QuiltMappingsExtension}'s {@link QuiltMappingsExtension#getMappingsVersion() mappingsVersion}
 *         </ul>
 *     <li> {@linkplain TaskCollection#configureEach(Action) configures}
 *          {@link TargetVersionConsumingTask}s with the following defaults:
 *          <ul>
 *              <li> {@link TargetVersionConsumingTask#getTargetVersion() targetVersion}:
 *                   {@link QuiltMappingsExtension}'s {@link QuiltMappingsExtension#getTargetVersion() targetVersion}
 *              <li> run {@link Task#onlyIf(Spec) onlyIf} their
 *                   {@link TargetVersionConsumingTask#getTargetVersion() targetVersion}
 *                   {@link Provider#isPresent() isPresent}
 *          </ul>
 */
public abstract class QuiltMappingsBasePlugin extends DefaultExtensionedMappingsProjectPlugin<QuiltMappingsExtension> {
    @Override
    protected QuiltMappingsExtension applyImpl(@NotNull Project project) {
        final var ext = project.getExtensions()
            .create(QuiltMappingsExtension.NAME, QuiltMappingsExtension.class);

        final BuildServiceRegistry services = project.getGradle().getSharedServices();

        final var enigmaProfile = services.registerIfAbsent(
            EnigmaProfileService.ENIGMA_PROFILE_SERVICE_NAME,
            EnigmaProfileService.class,
            spec -> spec.parameters(params -> {
                params.getProfileConfig().convention(ext.getEnigmaProfileConfig());
            })
        );

        final TaskContainer tasks = project.getTasks();

        // save this in a property so all tasks use the same cached value
        final var simpleTypeFieldNamePaths = this.getObjects().listProperty(String.class);
        simpleTypeFieldNamePaths.set(
            enigmaProfile
                .map(EnigmaProfileService::getProfile)
                .map(profile ->
                    profile.getServiceProfiles(JarIndexerService.TYPE).stream()
                        .flatMap(service -> service.getArgument(SIMPLE_TYPE_FIELD_NAMES_PATH).stream())
                        .map(stringOrStrings -> stringOrStrings.mapBoth(Stream::of, Collection::stream))
                        .flatMap(bothStringStreams ->
                            bothStringStreams.left().orElseGet(bothStringStreams::rightOrThrow)
                        )
                        .toList()
                )
        );

        tasks.withType(EnigmaProfileConsumingTask.class).configureEach(task -> {
            task.getEnigmaProfileService().convention(enigmaProfile);

            task.getEnigmaProfileConfig().convention(ext.getEnigmaProfileConfig());

            task.getProfileFileDependencies().from(simpleTypeFieldNamePaths);
        });

        this.provideDefaultError(
            ext.getEnigmaProfileConfig(),
            "No enigma profile specified. " +
                "A profile must be specified to use an " + EnigmaProfileConsumingTask.class.getSimpleName() + "."
        );

        final var mappingsDirOutputtingTasks = tasks.withType(MappingsDirOutputtingTask.class);

        mappingsDirOutputtingTasks.configureEach(task -> {
            task.getMappingsDir().convention(ext.getMappingsDir());
        });

        tasks.withType(MappingsDirConsumingTask.class).configureEach(task -> {
            task.getMappingsDir().convention(ext.getMappingsDir());

            task.getInputs().files(mappingsDirOutputtingTasks);
        });

        this.provideDefaultError(
            ext.getMappingsDir(),
            "No mappings directory specified. " +
                "A directory must be specified to use a " + MappingsDirConsumingTask.class.getSimpleName() + "."
        );

        tasks.withType(ArtifactFileTask.class).configureEach(task -> {
            this.provideDefaultError(
                task.getDestinationDirectory(),
                "No destination directory specified"
            );

            task.getDestinationDirectory().set(this.getBuildDir().dir("libs"));
        });

        tasks.withType(ArtifactFileTask.class)
            .matching(QuiltMappingsArtifactTask::isInstance)
            .configureEach(task -> {
                task.getArtifactBaseName().convention(Constants.MAPPINGS_NAME);

                task.getArtifactVersion().convention(ext.getMappingsVersion());
            });

        tasks.withType(AbstractArchiveTask.class)
            .matching(QuiltMappingsArtifactTask::isInstance)
            .configureEach(task -> {
                task.getArchiveBaseName().convention(Constants.MAPPINGS_NAME);

                task.getArchiveVersion().convention(ext.getMappingsVersion());
            });

        ext.getTargetVersion().set(
            this.getProviders().of(
                Version.TargetSource.class,
                spec -> spec.parameters(params ->
                    params.getMinecraftVersion().set(ext.getMinecraftVersion())
                )
            )
        );

        tasks.withType(TargetVersionConsumingTask.class).configureEach(task -> {
            task.getTargetVersion().convention(ext.getTargetVersion());

            task.onlyIf(unused -> task.getTargetVersion().isPresent());
        });

        return ext;
    }
}
