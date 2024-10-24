package quilt.internal.plugin;

import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.services.BuildServiceRegistry;
import org.gradle.api.tasks.TaskContainer;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.enigma.api.service.JarIndexerService;
import quilt.internal.QuiltMappingsExtension;
import quilt.internal.tasks.EnigmaProfileConsumingTask;
import quilt.internal.util.EnigmaProfileService;

import java.util.Collection;
import java.util.stream.Stream;

import static org.quiltmc.enigma_plugin.Arguments.SIMPLE_TYPE_FIELD_NAMES_PATH;

/**
 * <ul>
 *     <li> creates the {@value QuiltMappingsExtension#EXTENSION_NAME} extension
 *     <li> {@linkplain org.gradle.api.tasks.TaskContainer#register registers} the
 *          {@value EnigmaProfileService#ENIGMA_PROFILE_SERVICE_NAME} service
 *     <li> configures {@link EnigmaProfileConsumingTask}s
 * </ul>
 */
public abstract class QuiltMappingsBasePlugin implements MappingsProjectPlugin {
    @Override
    public void apply(@NotNull Project project) {
        final ObjectFactory objects = this.getObjects();

        final ExtensionContainer extensions = project.getExtensions();
        final var ext = extensions.create(QuiltMappingsExtension.EXTENSION_NAME, QuiltMappingsExtension.class);

        final BuildServiceRegistry services = project.getGradle().getSharedServices();

        final var enigmaProfile = services.registerIfAbsent(
            EnigmaProfileService.ENIGMA_PROFILE_SERVICE_NAME,
            EnigmaProfileService.class,
            spec -> spec.parameters(params -> {
                params.getProfileConfig().convention(ext.getEnigmaProfileConfig());
            })
        );

        final TaskContainer tasks = project.getTasks();

        {
            // save this in a property so all tasks use the same cached value
            final var simpleTypeFieldNamePaths = objects.listProperty(String.class);
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

                task.getSimpleTypeFieldNamesFiles().from(simpleTypeFieldNamePaths);
            });
        }

        this.provideDefaultError(
            ext.getEnigmaProfileConfig(),
            "No enigma profile specified. " +
                "A profile must be specified to use an " + EnigmaProfileConsumingTask.class.getSimpleName() + "."
        );
    }
}
