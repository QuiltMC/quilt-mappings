package quilt.internal.plugin;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.jetbrains.annotations.NotNull;
import quilt.internal.Constants;
import quilt.internal.plugin.abstraction.DefaultTaskedMappingsProjectPlugin;
import quilt.internal.tasks.build.AddProposedMappingsTask;
import quilt.internal.tasks.build.BuildMappingsTinyTask;
import quilt.internal.tasks.build.CompressTinyTask;
import quilt.internal.tasks.build.DropInvalidMappingsTask;
import quilt.internal.tasks.build.GeneratePackageInfoMappingsTask;
import quilt.internal.tasks.build.InvertPerVersionMappingsTask;
import quilt.internal.tasks.build.MergeTinyTask;
import quilt.internal.tasks.build.TinyJarTask;
import quilt.internal.tasks.jarmapping.MapJarTask;
import quilt.internal.tasks.jarmapping.MapPerVersionMappingsJarTask;
import quilt.internal.tasks.setup.DownloadMinecraftLibrariesTask;
import quilt.internal.tasks.setup.ExtractTinyMappingsTask;
import quilt.internal.tasks.setup.MergeJarsTask;
import quilt.internal.util.FileUtil;

import static quilt.internal.plugin.QuiltMappingsBasePlugin.ARCHIVE_FILE_NAME_PREFIX;

/**
 * {@linkplain TaskContainer#register Registers} tasks that map Minecraft jars.
 * <p>
 * Applies:
 * <ul>
 *     <li> {@link QuiltMappingsBasePlugin}
 *     <li> {@link MinecraftJarsPlugin}
 * </ul>
 * <p>
 * Additionally:
 * <ul>
 *     <li> creates the {@value PER_VERSION_MAPPINGS_CONFIGURATION_NAME} configuration,
 *          to which mappings must be added in order to use the
 *          {@value ExtractTinyMappingsTask#EXTRACT_TINY_PER_VERSION_MAPPINGS_TASK_NAME} task
 *     <li> if the {@link JavaPlugin} is applied, {@link org.gradle.api.Task#setEnabled(boolean) disables} the
 *          {@value JavaPlugin#JAR_TASK_NAME} task so its output doesn't collide with the
 *          {@value TinyJarTask#TINY_JAR_TASK_NAME} task's output
 *     <li> {@linkplain TaskCollection#configureEach(Action) configures} {@link MapJarTask}s
 *          {@link MapJarTask#getLibrariesDir() librariesDir}'s default values to
 *          {@value DownloadMinecraftLibrariesTask#DOWNLOAD_MINECRAFT_LIBRARIES_TASK_NAME}'s
 *          {@link DownloadMinecraftLibrariesTask#getLibrariesDir() librariesDir}
 * </ul>
 * Note:
 * <ul>
 *     <li> v2 mappings are created by {@link MapV2Plugin} tasks
 *     <li> {@value Constants#INTERMEDIARY_MAPPINGS_NAME} mappings are created by
 *          {@link MapIntermediaryPlugin} tasks
 * </ul>
 */
public abstract class MapMinecraftJarsPlugin extends DefaultTaskedMappingsProjectPlugin<MapMinecraftJarsPlugin.Tasks> {
    public static final String PER_VERSION_MAPPINGS_CONFIGURATION_NAME = Constants.PER_VERSION_MAPPINGS_NAME;

    @Override
    protected Tasks applyImpl(@NotNull Project project) {
        final Configuration perVersionMappings =
            project.getConfigurations().create(PER_VERSION_MAPPINGS_CONFIGURATION_NAME);

        // apply required plugins and save their registered objects
        final PluginContainer plugins = project.getPlugins();

        // configures MappingsDirOutputtingTasks (generatePackageInfoMappings)
        // configures MappingsDirConsumingTasks (buildMappingsTiny, dropInvalidMappings)
        plugins.apply(QuiltMappingsBasePlugin.class);

        final MinecraftJarsPlugin.Tasks minecraftJarsTasks =
            plugins.apply(MinecraftJarsPlugin.class).getTasks();
        final TaskProvider<DownloadMinecraftLibrariesTask> downloadMinecraftLibraries =
            minecraftJarsTasks.downloadMinecraftLibraries();
        final TaskProvider<MergeJarsTask> mergeJars =
            minecraftJarsTasks.mergeJars();

        // register this plugin's tasks
        final TaskContainer tasks = project.getTasks();

        final var extractTinyPerVersionMappings = tasks.register(
            ExtractTinyMappingsTask.EXTRACT_TINY_PER_VERSION_MAPPINGS_TASK_NAME,
            ExtractTinyMappingsTask.class,
            task -> {
                task.getZippedFile().convention(this.provideRequiredFile(perVersionMappings));

                task.getExtractionDest().convention(
                    this.provideMappingsDest(Constants.PER_VERSION_MAPPINGS_NAME, "tiny")
                );
            }
        );

        final var invertPerVersionMappings = tasks.register(
            InvertPerVersionMappingsTask.INVERT_PER_VERSION_MAPPINGS_TASK_NAME,
            InvertPerVersionMappingsTask.class,
            task -> {
                task.getInput().convention(
                    extractTinyPerVersionMappings.flatMap(ExtractTinyMappingsTask::getExtractionDest)
                );

                task.getInvertedTinyFile().convention(
                    this.provideMappingsDest(Constants.PER_VERSION_MAPPINGS_NAME + "-inverted", "tiny")
                );
            }
        );

        tasks.withType(MapJarTask.class).configureEach(task -> {
            task.getLibrariesDir().convention(
                downloadMinecraftLibraries
                    .flatMap(DownloadMinecraftLibrariesTask::getLibrariesDir)
            );
        });

        final var mapPerVersionMappingsJar = tasks.register(
            MapPerVersionMappingsJarTask.MAP_PER_VERSION_MAPPINGS_JAR_TASK_NAME,
            MapPerVersionMappingsJarTask.class,
            task -> {
                task.getInputJar().convention(
                    mergeJars.flatMap(MergeJarsTask::getMergedFile)
                );

                task.getMappingsFile().convention(
                    extractTinyPerVersionMappings.flatMap(ExtractTinyMappingsTask::getExtractionDest)
                );

                // TODO move this and other jars that are directly in the project dir to some sub dir
                task.getOutputJar().convention(this.getProjectDir().file(
                    Constants.MINECRAFT_VERSION + "-" + Constants.PER_VERSION_MAPPINGS_NAME + ".jar"
                ));
            }
        );

        final var buildMappingsTiny = tasks.register(
            BuildMappingsTinyTask.BUILD_MAPPINGS_TINY_TASK_NAME,
            BuildMappingsTinyTask.class,
            task -> {
                task.getPerVersionMappingsJar().convention(
                    mapPerVersionMappingsJar.flatMap(MapPerVersionMappingsJarTask::getOutputJar)
                );

                task.getOutputMappings().convention(
                    this.getMappingsDir().map(dir -> dir.file(Constants.MAPPINGS_NAME + ".tiny"))
                );
            }
        );

        final var insertAutoGeneratedMappings = tasks.register(
            AddProposedMappingsTask.INSERT_AUTO_GENERATED_MAPPINGS_TASK_NAME,
            AddProposedMappingsTask.class,
            task -> {
                task.getInputJar().convention(
                    mapPerVersionMappingsJar.flatMap(MapPerVersionMappingsJarTask::getOutputJar)
                );

                task.getInputMappings().convention(buildMappingsTiny.flatMap(BuildMappingsTinyTask::getOutputMappings));

                task.getOutputMappings().convention(
                    this.getMappingsDir().zip(task.getInputMappings(), (dir, input) ->
                        dir.file(FileUtil.getNameWithExtension(input, "-inserted.tiny"))
                    )
                );

                task.getPreprocessedMappings().convention(
                    this.getTempDir().zip(task.getInputMappings(), (dir, input) ->
                        dir.file(FileUtil.getNameWithExtension(input, "-preprocessed.tiny"))
                    )
                );

                task.getProcessedMappings().convention(
                    this.getTempDir().zip(task.getInputMappings(), (dir, input) ->
                        dir.file(FileUtil.getNameWithExtension(input, "-processed.tiny"))
                    )
                );
            }
        );

        final var mergeTiny = tasks.register(
            MergeTinyTask.MERGE_TINY_TASK_NAME,
            MergeTinyTask.class,
            task -> {
                task.getInput().convention(buildMappingsTiny.flatMap(BuildMappingsTinyTask::getOutputMappings));

                task.getHashedTinyMappings().convention(
                    invertPerVersionMappings.flatMap(InvertPerVersionMappingsTask::getInvertedTinyFile)
                );

                task.getOutputMappings().convention(this.getMappingsDir().map(dir -> dir.file("mappings.tiny")));
            }
        );

        final var tinyJar = tasks.register(
            TinyJarTask.TINY_JAR_TASK_NAME,
            TinyJarTask.class,
            task -> {
                task.getMappings().convention(mergeTiny.flatMap(MergeTinyTask::getOutputMappings));

                task.getArchiveFileName().convention(ARCHIVE_FILE_NAME_PREFIX + ".jar");

                task.getDestinationDirectory().convention(this.getLibsDir());
            }
        );

        plugins.withType(JavaPlugin.class, java -> {
            // Its artifact collides with the `tinyJar` one, just disable it since it isn't used either way
            tasks.named(JavaPlugin.JAR_TASK_NAME).configure(task -> task.setEnabled(false));
        });

        tasks.register(
            CompressTinyTask.COMPRESS_TINY_TASK_NAME,
            CompressTinyTask.class,
            task -> {
                task.getMappings().convention(mergeTiny.flatMap(MergeTinyTask::getOutputMappings));

                task.getCompressedTiny().convention(
                    tinyJar.flatMap(TinyJarTask::getDestinationDirectory)
                        .map(dir -> dir.file(ARCHIVE_FILE_NAME_PREFIX + "-tiny.gz"))
                );
            }
        );

        tasks.register(
            GeneratePackageInfoMappingsTask.GENERATE_PACKAGE_INFO_MAPPINGS_TASK_NAME,
            GeneratePackageInfoMappingsTask.class,
            task -> {
                task.getInputJar().convention(mapPerVersionMappingsJar.flatMap(MapPerVersionMappingsJarTask::getOutputJar));
            }
        );

        tasks.register(
            DropInvalidMappingsTask.DROP_INVALID_MAPPINGS_TASK_NAME,
            DropInvalidMappingsTask.class,
            task -> {
                task.getPerVersionMappingsJar().convention(
                    mapPerVersionMappingsJar.flatMap(MapPerVersionMappingsJarTask::getOutputJar)
                );
            }
        );

        return new Tasks(mapPerVersionMappingsJar, invertPerVersionMappings, insertAutoGeneratedMappings);
    }

    public record Tasks(
        TaskProvider<MapPerVersionMappingsJarTask> mapPerVersionMappingsJar,
        TaskProvider<InvertPerVersionMappingsTask> invertPerVersionMappings,
        TaskProvider<AddProposedMappingsTask> insertAutoGeneratedMappings
    ) { }
}
