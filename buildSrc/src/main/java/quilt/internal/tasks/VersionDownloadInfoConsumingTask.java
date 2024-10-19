package quilt.internal.tasks;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import quilt.internal.util.VersionDownloadInfo;

/**
 * TODO javadoc
 */
public interface VersionDownloadInfoConsumingTask extends MappingsTask {
    @Input
    Property<VersionDownloadInfo> getVersionDownloadInfo();
}
