package quilt.internal.tasks;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import quilt.internal.util.VersionDownloadInfo;

/**
 * A task that takes {@link VersionDownloadInfo} as input.
 * <p>
 * {@link quilt.internal.plugin.MinecraftJarsPlugin MinecraftJarsPlugin}
 * {@linkplain org.gradle.api.tasks.TaskCollection#configureEach configures}
 * the default value of {@link #getVersionDownloadInfo versionDownloadInfo} to
 * {@value quilt.internal.tasks.setup.DownloadWantedVersionManifestTask#DOWNLOAD_WANTED_VERSION_MANIFEST_TASK_NAME}'s
 * {@linkplain quilt.internal.tasks.setup.DownloadWantedVersionManifestTask#provideVersionDownloadInfo provided}
 * {@link VersionDownloadInfo}
 */
public interface VersionDownloadInfoConsumingTask extends MappingsTask {
    @Input
    Property<VersionDownloadInfo> getVersionDownloadInfo();
}
