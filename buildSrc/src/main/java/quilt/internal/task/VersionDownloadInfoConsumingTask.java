package quilt.internal.task;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskCollection;
import quilt.internal.plugin.MinecraftJarsPlugin;
import quilt.internal.util.VersionDownloadInfo;

/**
 * A task that takes {@link VersionDownloadInfo} as input.
 * <p>
 * {@link MinecraftJarsPlugin} {@linkplain TaskCollection#configureEach configures} some defaults.
 */
public interface VersionDownloadInfoConsumingTask extends MappingsTask {
    @Input
    Property<VersionDownloadInfo> getVersionDownloadInfo();
}
