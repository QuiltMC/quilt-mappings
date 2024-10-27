package quilt.internal.plugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.RegularFile;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.jetbrains.annotations.NotNull;
import quilt.internal.Constants;
import quilt.internal.QuiltMappingsExtension;
import quilt.internal.plugin.abstraction.MappingsProjectPlugin;
import quilt.internal.task.build.BuildIntermediaryTask;
import quilt.internal.task.build.IntermediaryMappingsV2JarTask;
import quilt.internal.task.build.MergeIntermediaryTask;
import quilt.internal.task.build.MergeTinyV2Task;
import quilt.internal.task.build.RemoveIntermediaryTask;
import quilt.internal.task.setup.ExtractTinyIntermediaryMappingsTask;
import quilt.internal.task.setup.ExtractTinyMappingsTask;
import quilt.internal.task.setup.IntermediaryDependantTask;

/**
 * {@linkplain TaskContainer#register Registers} tasks related to
 * {@value Constants#INTERMEDIARY_MAPPINGS_NAME} mappings.
 * <p>
 * Applies:
 * <ul>
 *     <li> {@link QuiltMappingsBasePlugin}
 *     <li> {@link MapV2Plugin}
 * </ul>
 * <p>
 * Additionally:
 * <ul>
 *     <li> {@linkplain  TaskContainer#register registers} the
 *          {@value BuildIntermediaryTask#BUILD_INTERMEDIARY_TASK_NAME} aggregate task
 *     <li> {@linkplain org.gradle.api.tasks.TaskCollection#configureEach configures} {@link IntermediaryDependantTask}s
 *          to run {@link Task#onlyIf(Spec) onlyIf}
 *          the {@value MapIntermediaryPlugin#INTERMEDIARY_MAPPINGS_CONFIGURATION_NAME}
 *          {@link org.gradle.api.artifacts.Configuration Configuration}
 *          successfully {@linkplain Configuration#resolve() resolves}
 * </ul>
 */
public abstract class MapIntermediaryPlugin implements MappingsProjectPlugin {
    public static final String INTERMEDIARY_MAPPINGS_CONFIGURATION_NAME = Constants.INTERMEDIARY_MAPPINGS_NAME;

    @Override
    public void apply(@NotNull Project project) {
        final ConfigurationContainer configurations = project.getConfigurations();
        final Configuration intermediaryMappings = configurations.create(INTERMEDIARY_MAPPINGS_CONFIGURATION_NAME);

        // apply required plugins and save their registered objects
        final PluginContainer plugins = project.getPlugins();

        final QuiltMappingsExtension ext = plugins.apply(QuiltMappingsBasePlugin.class).getExt();

        final MapV2Plugin.Tasks mappingsV2Tasks =
            plugins.apply(MapV2Plugin.class).getTasks();
        final TaskProvider<MergeTinyV2Task> mergeTinyV2 =
            mappingsV2Tasks.mergeTinyV2();

        // register this plugin's tasks
        final TaskContainer tasks = project.getTasks();

        final var extractTinyIntermediaryMappings = tasks.register(
            ExtractTinyIntermediaryMappingsTask.EXTRACT_TINY_INTERMEDIARY_MAPPINGS_TASK_NAME,
            ExtractTinyIntermediaryMappingsTask.class,
            task -> {
                task.getExtractionDest().convention(
                    this.provideMappingsDest(Constants.INTERMEDIARY_MAPPINGS_NAME, "tiny")
                );
            }
        );

        {
            final Provider<RegularFile> intermediaryFile = this.provideOptionalFile(intermediaryMappings);

            tasks.withType(IntermediaryDependantTask.class).configureEach(task -> {
                task.onlyIf(unused -> intermediaryFile.isPresent());
            });

            extractTinyIntermediaryMappings.configure(task -> {
                task.getZippedFile().convention(intermediaryFile);
            });
        }

        final var mergeIntermediary = tasks.register(
            MergeIntermediaryTask.MERGE_INTERMEDIARY_TASK_NAME,
            MergeIntermediaryTask.class,
            task -> {
                task.getInput().convention(
                    extractTinyIntermediaryMappings.flatMap(ExtractTinyMappingsTask::getExtractionDest)
                );

                task.getMergedTinyMappings().convention(mergeTinyV2.flatMap(MergeTinyV2Task::getOutputMappings));

                task.getOutputMappings().convention(
                    this.getMappingsDir().map(dir -> dir.file("mappings-intermediaryMerged.tiny"))
                );
            }
        );

        final var removeIntermediary = tasks.register(
            RemoveIntermediaryTask.REMOVE_INTERMEDIARY_TASK_NAME,
            RemoveIntermediaryTask.class,
            task -> {
                task.getInput().convention(mergeIntermediary.flatMap(MergeIntermediaryTask::getOutputMappings));

                task.getOutputMappings().convention(
                    this.getMappingsDir().map(dir -> dir.file("mappings-intermediary.tiny"))
                );
            }
        );

        final var intermediaryV2MappingsJar = tasks.register(
            IntermediaryMappingsV2JarTask.INTERMEDIARY_V_2_MAPPINGS_JAR_TASK_NAME,
            IntermediaryMappingsV2JarTask.class,
            ext.getUnpickVersion()
        );
        intermediaryV2MappingsJar.configure(task -> {
            task.getMappings().convention(removeIntermediary.flatMap(RemoveIntermediaryTask::getOutputMappings));

            task.getArchiveClassifier().convention(IntermediaryMappingsV2JarTask.CLASSIFIER);
        });

        final var intermediaryV2MergedMappingsJar = tasks.register(
            IntermediaryMappingsV2JarTask.INTERMEDIARY_V_2_MERGED_MAPPINGS_JAR_TASK_NAME,
            IntermediaryMappingsV2JarTask.class,
            ext.getUnpickVersion()
        );
        intermediaryV2MergedMappingsJar.configure(task -> {
            task.getMappings().convention(mergeIntermediary.flatMap(MergeIntermediaryTask::getOutputMappings));

            task.getArchiveClassifier().convention(IntermediaryMappingsV2JarTask.MERGED_CLASSIFIER);
        });

        tasks.register(
            BuildIntermediaryTask.BUILD_INTERMEDIARY_TASK_NAME,
            BuildIntermediaryTask.class,
            task -> {
                task.dependsOn(intermediaryV2MappingsJar, intermediaryV2MergedMappingsJar);
            }
        );
    }
}
