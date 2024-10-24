package quilt.internal.plugin;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.jetbrains.annotations.NotNull;
import quilt.internal.Constants;
import quilt.internal.tasks.VersionDownloadInfoConsumingTask;
import quilt.internal.tasks.setup.DownloadMinecraftJarsTask;
import quilt.internal.tasks.setup.DownloadMinecraftLibrariesTask;
import quilt.internal.tasks.setup.DownloadVersionsManifestTask;
import quilt.internal.tasks.setup.DownloadWantedVersionManifestTask;
import quilt.internal.tasks.setup.ExtractServerJarTask;
import quilt.internal.tasks.setup.MergeJarsTask;
import quilt.internal.util.VersionDownloadInfo;

/**
 * {@link TaskContainer#register Registers} tasks that download and extract Minecraft's client, server, and library jars.
 * <p>
 * Additionally:
 * <ul>
 *     <li> {@link TaskContainer#register registers} {@value MergeJarsTask#MERGE_JARS_TASK_NAME}
 *          which merges the client and server jars
 *     <li> {@linkplain org.gradle.api.tasks.TaskCollection#configureEach configures}
 *          {@link VersionDownloadInfoConsumingTask}s
 * </ul>
 */
public abstract class MinecraftJarsPlugin implements MappingsProjectPlugin {
    @Override
    public void apply(@NotNull Project project) {
        final ObjectFactory objects = this.getObjects();

        final Directory projectDir = this.getProjectDir();

        final Provider<Directory> minecraftDir = this.getMinecraftDir();

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
                        downloadVersionsManifest.flatMap(DownloadVersionsManifestTask::provideVersionEntry)
                    );

                    task.getDest().convention(
                        minecraftDir.map(dir -> dir.file(Constants.MINECRAFT_VERSION + ".json"))
                    );
                }
            );

            // put mapped provider in a property so all tasks use the same cached value
            final Provider<VersionDownloadInfo> versionDownloadInfo =
                objects.property(VersionDownloadInfo.class).convention(
                    downloadWantedVersionManifest.flatMap(DownloadWantedVersionManifestTask::provideVersionDownloadInfo)
                );

            tasks.withType(VersionDownloadInfoConsumingTask.class).configureEach(task -> {
                task.getVersionDownloadInfo().convention(versionDownloadInfo);
            });
        }

        final var downloadMinecraftJars = tasks.register(
            DownloadMinecraftJarsTask.DOWNLOAD_MINECRAFT_JARS_TASK_NAME,
            DownloadMinecraftJarsTask.class,
            task -> {
                task.getClientJar().convention(
                    minecraftDir.map(dir -> dir.file(Constants.MINECRAFT_VERSION + "-client.jar"))
                );

                task.getServerBootstrapJar().convention(
                    minecraftDir.map(dir -> dir.file(Constants.MINECRAFT_VERSION + "-server-bootstrap.jar"))
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
                    minecraftDir.map(dir -> dir.file(Constants.MINECRAFT_VERSION + "-server.jar"))
                );
            }
        );

        tasks.register(
            MergeJarsTask.MERGE_JARS_TASK_NAME,
            MergeJarsTask.class,
            task -> {
                task.getClientJar().convention(downloadMinecraftJars.flatMap(DownloadMinecraftJarsTask::getClientJar));

                task.getServerJar().convention(extractServerJar.flatMap(ExtractServerJarTask::getExtractionDest));

                // TODO move this and other jars that are directly in the project dir to some sub dir
                task.getMergedFile().convention(projectDir.file(Constants.MINECRAFT_VERSION + "-merged.jar"));
            }
        );

        tasks.register(
            DownloadMinecraftLibrariesTask.DOWNLOAD_MINECRAFT_LIBRARIES_TASK_NAME,
            DownloadMinecraftLibrariesTask.class,
            task -> {
                task.getLibrariesDir().convention(minecraftDir.map(dir -> dir.dir("libraries")));
            }
        );
    }
}
