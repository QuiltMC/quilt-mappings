package quilt.internal.task;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import quilt.internal.plugin.MinecraftJarsPlugin;
import quilt.internal.util.VersionDownloadInfo;

/**
 * A task that takes {@link VersionDownloadInfo} as input.
 *
 * @see MinecraftJarsPlugin MinecraftJarsPlugin's configureEach
 */
public interface VersionDownloadInfoConsumingTask extends MappingsTask {
    @Input
    Property<VersionDownloadInfo> getVersionDownloadInfo();
}
