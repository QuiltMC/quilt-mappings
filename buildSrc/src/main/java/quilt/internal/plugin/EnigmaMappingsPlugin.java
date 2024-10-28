package quilt.internal.plugin;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.enigma.api.Enigma;
import quilt.internal.plugin.abstraction.MappingsProjectPlugin;
import quilt.internal.task.jarmapping.MapJarTask;
import quilt.internal.task.jarmapping.MapPerVersionMappingsJarTask;
import quilt.internal.task.mappings.AbstractEnigmaMappingsTask;
import quilt.internal.task.mappings.EnigmaMappingsServerTask;
import quilt.internal.task.mappings.EnigmaMappingsTask;
import quilt.internal.task.unpick.UnpickJarTask;

import static quilt.internal.util.ProviderUtil.toOptional;

/**
 * {@linkplain TaskContainer#register Registers} tasks that launch {@link Enigma} servers and GUIs.
 * <p>
 * These are tools for editing mappings files.
 * <p>
 * Additionally:
 * <ul>
 *     <li> {@linkplain ConfigurationContainer#create(String) creates} the
 *          {@value ENIGMA_RUNTIME_CONFIGURATION_NAME} {@link Configuration}
 *     <li> {@linkplain TaskCollection#configureEach(Action) configures}
 *          {@link AbstractEnigmaMappingsTask}s with the following defaults:
 *          <ul>
 *              <li> adds the {@value ENIGMA_RUNTIME_CONFIGURATION_NAME} {@link Configuration}
 *                   to the {@link AbstractEnigmaMappingsTask#classpath(Object...) classpath}
 *              <li> {@linkplain AbstractEnigmaMappingsTask#jvmArgs tells the JVM} to allocate
 *                   {@linkplain #DEFAULT_ENIGMA_JVM_MEMORY_ARG 2Gb of memory} for {@link Enigma}
 *          </ul>
 *     <li> {@linkplain TaskCollection#configureEach(Action) configures} {@link EnigmaMappingsServerTask}s
 *          to search the following gradle properties for default values:
 *          <ul>
 *              <li> {@value #ENIGMA_SERVER_PORT_PROP} for the
 *                   {@link EnigmaMappingsServerTask#getPort() port}
 *              <li> {@value #ENIGMA_SERVER_PASSWORD_PROP} for the
 *                   {@link EnigmaMappingsServerTask#getPassword() password}
 *              <li> {@value #ENIGMA_SERVER_LOG_PROP} for the
 *                   {@link EnigmaMappingsServerTask#getLog() log} path
 *              <li> {@value #ENIGMA_SERVER_ARGS_PROP} for any additional command line args
 *          </ul>
 * </ul>
 */
public abstract class EnigmaMappingsPlugin implements MappingsProjectPlugin {
    public static final String ENIGMA_RUNTIME_CONFIGURATION_NAME = "enigmaRuntime";

    public static final String DEFAULT_ENIGMA_JVM_MEMORY_ARG = "-Xmx2048m";

    private static final String ENIGMA_SERVER_PROP_PREFIX = "enigma_server_";
    public static final String ENIGMA_SERVER_ARGS_PROP = ENIGMA_SERVER_PROP_PREFIX + "args";
    public static final String ENIGMA_SERVER_LOG_PROP =
        ENIGMA_SERVER_PROP_PREFIX + EnigmaMappingsServerTask.LOG_OPTION;
    public static final String ENIGMA_SERVER_PASSWORD_PROP =
        ENIGMA_SERVER_PROP_PREFIX + EnigmaMappingsServerTask.PASSWORD_OPTION;
    public static final String ENIGMA_SERVER_PORT_PROP =
        ENIGMA_SERVER_PROP_PREFIX + EnigmaMappingsServerTask.PORT_OPTION;

    @Override
    public void apply(@NotNull Project project) {
        final Configuration enigmaRuntime = project.getConfigurations().create(ENIGMA_RUNTIME_CONFIGURATION_NAME);

        final PluginContainer plugins = project.getPlugins();

        // configures EnigmaProfileConsumingTasks + MappingsDirConsumingTasks
        //  (mappings, mappingsUnpicked, mappingsServer, mappingsUnpickedServer)
        plugins.apply(QuiltMappingsBasePlugin.class).getExt();

        final MapMinecraftJarsPlugin.Tasks mapMinecraftJarsTasks =
            plugins.apply(MapMinecraftJarsPlugin.class).getTasks();
        final TaskProvider<MapPerVersionMappingsJarTask> mapPerVersionMappingsJar =
            mapMinecraftJarsTasks.mapPerVersionMappingsJar();

        final MapV2Plugin.Tasks mappingsV2Tasks =
            plugins.apply(MapV2Plugin.class).getTasks();
        final TaskProvider<UnpickJarTask> unpickHashedJar =
            mappingsV2Tasks.unpickHashedJar();

        final TaskContainer tasks = project.getTasks();

        tasks.withType(AbstractEnigmaMappingsTask.class).configureEach(task -> {
            task.classpath(enigmaRuntime);

            task.jvmArgs(DEFAULT_ENIGMA_JVM_MEMORY_ARG);
        });

        tasks.register(
            EnigmaMappingsTask.MAPPINGS_TASK_NAME,
            EnigmaMappingsTask.class,
            task -> {
                task.getJarToMap().convention(
                    mapPerVersionMappingsJar.flatMap(MapPerVersionMappingsJarTask::getOutputJar)
                );
            }
        );

        tasks.register(
            EnigmaMappingsTask.MAPPINGS_UNPICKED_TASK_NAME,
            EnigmaMappingsTask.class,
            task -> {
                task.getJarToMap().convention(unpickHashedJar.flatMap(UnpickJarTask::getOutputFile));
            }
        );

        tasks.withType(EnigmaMappingsServerTask.class).configureEach(task -> {
            final ProviderFactory providers = this.getProviders();

            task.getPort().convention(
                providers.gradleProperty(ENIGMA_SERVER_PORT_PROP)
            );

            task.getPassword().convention(
                providers.gradleProperty(ENIGMA_SERVER_PASSWORD_PROP)
            );

            task.getLog().convention(
                providers.gradleProperty(ENIGMA_SERVER_LOG_PROP)
                    .map(this.getProjectDir()::file)
                    .orElse(this.getBuildDir().file("logs/server.log"))
            );

            toOptional(
                providers.gradleProperty(ENIGMA_SERVER_ARGS_PROP).map(args -> args.split(" "))
            ).ifPresent(task::args);
        });

        tasks.register(
            EnigmaMappingsServerTask.MAPPINGS_SERVER_TASK_NAME,
            EnigmaMappingsServerTask.class,
            task -> {
                task.getJarToMap().convention(mapPerVersionMappingsJar.flatMap(MapJarTask::getOutputJar));
            }
        );

        tasks.register(
            EnigmaMappingsServerTask.MAPPINGS_UNPICKED_SERVER_TASK_NAME,
            EnigmaMappingsServerTask.class,
            task -> {
                task.getJarToMap().convention(unpickHashedJar.flatMap(UnpickJarTask::getOutputFile));
            }
        );
    }
}
