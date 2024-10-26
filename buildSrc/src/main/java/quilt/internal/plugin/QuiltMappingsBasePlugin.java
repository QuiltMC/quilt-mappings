package quilt.internal.plugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.services.BuildServiceRegistry;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.enigma.api.service.JarIndexerService;
import quilt.internal.Constants;
import quilt.internal.QuiltMappingsExtension;
import quilt.internal.plugin.abstraction.MappingsProjectPlugin;
import quilt.internal.tasks.ArtifactFileTask;
import quilt.internal.tasks.EnigmaProfileConsumingTask;
import quilt.internal.tasks.MappingsDirConsumingTask;
import quilt.internal.tasks.QuiltMappingsArtifactTask;
import quilt.internal.tasks.mappings.MappingsDirOutputtingTask;
import quilt.internal.util.EnigmaProfileService;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import static org.quiltmc.enigma_plugin.Arguments.SIMPLE_TYPE_FIELD_NAMES_PATH;

/**
 * Base Quilt Mappings plugin.
 * <p>
 * Adds no tasks, but performs setup and {@linkplain TaskCollection#configureEach configures} several types of tasks:
 * <ul>
 *     <li> creates the {@value QuiltMappingsExtension#EXTENSION_NAME} extension
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
 *              <li> {@link EnigmaProfileConsumingTask#getSimpleTypeFieldNamesFiles() simpleTypeFieldNamesFiles}:
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
 *                  {@link Constants#MAPPINGS_VERSION MAPPINGS_VERSION}
 *         </ul>
 */
public abstract class QuiltMappingsBasePlugin implements MappingsProjectPlugin {
    static final String MAPPINGS_NAME_PREFIX = Constants.MAPPINGS_NAME + "-";

    @Nullable
    private QuiltMappingsExtension ext;

    @Override
    public void apply(@NotNull Project project) {
        this.ext = project.getExtensions()
            .create(QuiltMappingsExtension.EXTENSION_NAME, QuiltMappingsExtension.class);

        final BuildServiceRegistry services = project.getGradle().getSharedServices();

        final var enigmaProfile = services.registerIfAbsent(
            EnigmaProfileService.ENIGMA_PROFILE_SERVICE_NAME,
            EnigmaProfileService.class,
            spec -> spec.parameters(params -> {
                params.getProfileConfig().convention(this.ext.getEnigmaProfileConfig());
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

            task.getEnigmaProfileConfig().convention(this.ext.getEnigmaProfileConfig());

            task.getSimpleTypeFieldNamesFiles().from(simpleTypeFieldNamePaths);
        });

        this.provideDefaultError(
            this.ext.getEnigmaProfileConfig(),
            "No enigma profile specified. " +
                "A profile must be specified to use an " + EnigmaProfileConsumingTask.class.getSimpleName() + "."
        );

        final var mappingsDirOutputtingTasks = tasks.withType(MappingsDirOutputtingTask.class);

        mappingsDirOutputtingTasks.configureEach(task -> {
            task.getMappingsDir().convention(this.ext.getMappingsDir());
        });

        tasks.withType(MappingsDirConsumingTask.class).configureEach(task -> {
            task.getMappingsDir().convention(this.ext.getMappingsDir());

            task.getInputs().files(mappingsDirOutputtingTasks);
        });

        this.provideDefaultError(
            this.ext.getMappingsDir(),
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

                task.getArtifactVersion().convention(Constants.MAPPINGS_VERSION);
            });

        tasks.withType(AbstractArchiveTask.class)
            .matching(QuiltMappingsArtifactTask::isInstance)
            .configureEach(task -> {
                task.getArchiveBaseName().convention(Constants.MAPPINGS_NAME);

                task.getArchiveVersion().convention(Constants.MAPPINGS_VERSION);
            });
    }

    public QuiltMappingsExtension getExt() {
        return Objects.requireNonNull(this.ext, "Extension not yet registered");
    }
}
