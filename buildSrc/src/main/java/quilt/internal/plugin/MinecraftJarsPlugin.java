package quilt.internal.plugin;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.jetbrains.annotations.NotNull;
import quilt.internal.QuiltMappingsExtension;
import quilt.internal.plugin.abstraction.DefaultTaskedMappingsProjectPlugin;
import quilt.internal.task.VersionParserConsumingTask;
import quilt.internal.task.setup.DownloadMinecraftJarsTask;
import quilt.internal.task.setup.DownloadMinecraftLibrariesTask;
import quilt.internal.task.setup.DownloadVersionsManifestTask;
import quilt.internal.task.setup.DownloadWantedVersionManifestTask;
import quilt.internal.task.setup.ExtractServerJarTask;
import quilt.internal.task.setup.MergeJarsTask;
import quilt.internal.util.serializable.SerializableVersionEntry;
import quilt.internal.util.serializable.VersionParser;

/**
 * {@linkplain TaskContainer#register Registers} tasks that download and extract
 * Minecraft's client, server, and library jars.
 * <p>
 * Additionally:
 * <ul>
 *     <li> {@link TaskContainer#register registers} {@value MergeJarsTask#MERGE_JARS_TASK_NAME}
 *          which merges the client and server jars
 *     <li> {@linkplain org.gradle.api.tasks.TaskCollection#configureEach configures} the default value of
 *          {@link VersionParserConsumingTask}s'
 *          {@link VersionParserConsumingTask#getVersionParser versionParser} to
 *          {@value DownloadWantedVersionManifestTask#DOWNLOAD_WANTED_VERSION_MANIFEST_TASK_NAME}'s
 *          {@linkplain DownloadWantedVersionManifestTask#provideVersionParser provided}
 *          {@link VersionParser}
 * </ul>
 */
public abstract class MinecraftJarsPlugin extends DefaultTaskedMappingsProjectPlugin<MinecraftJarsPlugin.Tasks> {
    @Override
    protected Tasks applyImpl(@NotNull Project project) {
        final Provider<Directory> minecraftDir = this.getMinecraftDir();

        final PluginContainer plugins = project.getPlugins();

        final QuiltMappingsExtension ext = plugins.apply(QuiltMappingsBasePlugin.class).getExt();

        final TaskContainer tasks = project.getTasks();

        final var downloadVersionsManifest = tasks.register(
            DownloadVersionsManifestTask.DOWNLOAD_VERSIONS_MANIFEST_TASK_NAME,
            DownloadVersionsManifestTask.class,
            task -> {
                task.getDest().convention(minecraftDir.map(dir -> dir.file("version_manifest_v2.json")));
            }
        );

        {
            final var downloadWantedVersionManifest = tasks.register(
                DownloadWantedVersionManifestTask.DOWNLOAD_WANTED_VERSION_MANIFEST_TASK_NAME,
                DownloadWantedVersionManifestTask.class,
                task -> {
                    task.getManifestVersion().convention(
                        downloadVersionsManifest.flatMap(DownloadVersionsManifestTask::getDest)
                            .map(RegularFile::getAsFile)
                            .zip(ext.getMinecraftVersion(), SerializableVersionEntry::of)
                    );

                    task.getDest().convention(
                        minecraftDir.flatMap(dir -> dir.file(ext.provideSuffixedMinecraftVersion(".json")))
                    );
                }
            );

            // put mapped provider in a property so all tasks use the same cached value
            final Property<VersionParser> versionParser = this.getObjects().property(VersionParser.class);
            versionParser.set(
                downloadWantedVersionManifest.flatMap(DownloadWantedVersionManifestTask::provideVersionParser)
            );

            tasks.withType(VersionParserConsumingTask.class).configureEach(task -> {
                task.getVersionParser().convention(versionParser);
            });
        }

        final var downloadMinecraftJars = tasks.register(
            DownloadMinecraftJarsTask.DOWNLOAD_MINECRAFT_JARS_TASK_NAME,
            DownloadMinecraftJarsTask.class,
            task -> {
                task.getClientJar().convention(
                    minecraftDir.flatMap(dir -> dir.file(ext.provideSuffixedMinecraftVersion("-client.jar")))
                );

                task.getServerBootstrapJar().convention(
                    minecraftDir.flatMap(dir -> dir.file(ext.provideSuffixedMinecraftVersion("-server-bootstrap.jar")))
                );
            }
        );

        final var extractServerJar = tasks.register(
            ExtractServerJarTask.EXTRACT_SERVER_JAR_TASK_NAME,
            ExtractServerJarTask.class,
            task -> {
                task.getZippedFile().convention(
                    downloadMinecraftJars.flatMap(DownloadMinecraftJarsTask::getServerBootstrapJar)
                );

                task.getExtractionDest().convention(
                    minecraftDir.flatMap(dir -> dir.file(ext.provideSuffixedMinecraftVersion("-server.jar")))
                );
            }
        );

        final var mergeJars = tasks.register(
            MergeJarsTask.MERGE_JARS_TASK_NAME,
            MergeJarsTask.class,
            task -> {
                task.getClientJar().convention(downloadMinecraftJars.flatMap(DownloadMinecraftJarsTask::getClientJar));

                task.getServerJar().convention(extractServerJar.flatMap(ExtractServerJarTask::getExtractionDest));

                // TODO move this and other jars that are directly in the project dir to some sub dir
                task.getMergedFile().convention(
                    this.getProjectDir().file(ext.provideSuffixedMinecraftVersion("-merged.jar"))
                );
            }
        );

        final var downloadMinecraftLibraries = tasks.register(
            DownloadMinecraftLibrariesTask.DOWNLOAD_MINECRAFT_LIBRARIES_TASK_NAME,
            DownloadMinecraftLibrariesTask.class,
            task -> {
                task.getLibrariesDir().convention(minecraftDir.map(dir -> dir.dir("libraries")));
            }
        );

        return new Tasks(mergeJars, downloadMinecraftLibraries);
    }

    public record Tasks(
        TaskProvider<MergeJarsTask> mergeJars,
        TaskProvider<DownloadMinecraftLibrariesTask> downloadMinecraftLibraries
    ) { }
}
