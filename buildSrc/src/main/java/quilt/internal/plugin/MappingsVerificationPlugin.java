package quilt.internal.plugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.jetbrains.annotations.NotNull;
import quilt.internal.QuiltMappingsExtension;
import quilt.internal.plugin.abstraction.MappingsProjectPlugin;
import quilt.internal.task.jarmapping.MapPerVersionMappingsJarTask;
import quilt.internal.task.lint.Checker;
import quilt.internal.task.lint.FindDuplicateMappingFilesTask;
import quilt.internal.task.lint.MappingLintTask;

/**
 * {@linkplain TaskContainer#register Registers} tasks that verify the mappings in {@link QuiltMappingsExtension}'s
 * {@link QuiltMappingsExtension#getMappingsDir() mappingsDir}.
 * <p>
 * Applies:
 * <ul>
 *     <li> {@link QuiltMappingsBasePlugin}
 *     <li> {@link MapMinecraftJarsPlugin}
 * </ul>
 *
 * If {@link LifecycleBasePlugin} is applied, configures the {@value LifecycleBasePlugin#CHECK_TASK_NAME} task
 * to {@linkplain Task#dependsOn(Object...) depend on} the {@value MappingLintTask#MAPPING_LINT_TASK_NAME} task,
 * which in turn {@linkplain Task#dependsOn(Object...) depend on} the
 * {@value FindDuplicateMappingFilesTask#FIND_DUPLICATE_MAPPING_FILES_TASK_NAME} task.
 */
public abstract class MappingsVerificationPlugin implements MappingsProjectPlugin {
    @Override
    public void apply(@NotNull Project project) {
        // apply required plugins and save their registered objects
        final PluginContainer plugins = project.getPlugins();

        // adds check task
        plugins.apply(LifecycleBasePlugin.class);

        // configures MappingsDirConsumingTasks (findDuplicateMappingFiles, mappingLint)
        plugins.apply(QuiltMappingsBasePlugin.class);

        final MapMinecraftJarsPlugin.Tasks mapMinecraftJarsTasks =
            plugins.apply(MapMinecraftJarsPlugin.class).getTasks();
        final TaskProvider<MapPerVersionMappingsJarTask> mapPerVersionMappingsJar =
            mapMinecraftJarsTasks.mapPerVersionMappingsJar();

        // register this plugin's tasks
        final TaskContainer tasks = project.getTasks();

        final var findDuplicateMappingFiles = tasks.register(
            FindDuplicateMappingFilesTask.FIND_DUPLICATE_MAPPING_FILES_TASK_NAME,
            FindDuplicateMappingFilesTask.class
        );

        final var mappingLint = tasks.register(
            MappingLintTask.MAPPING_LINT_TASK_NAME,
            MappingLintTask.class,
            task -> {
                // this does mappings verification but has no output to depend on
                task.dependsOn(findDuplicateMappingFiles);

                task.getJarFile().convention(
                    mapPerVersionMappingsJar.flatMap(MapPerVersionMappingsJarTask::getOutputJar)
                );

                task.getCheckers().addAll(Checker.DEFAULT_CHECKERS);

                this.provideDefaultError(
                    task.getDictionaryFile(),
                    "No dictionary file specified. A file must be specified to use " + task.getName()
                );
            }
        );

        plugins.withType(LifecycleBasePlugin.class, lifecycle -> {
            tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure(task -> task.dependsOn(mappingLint));
        });
    }
}
