package quilt.internal.tasks;

import org.gradle.api.Task;
import quilt.internal.util.DownloadImmediate;

// TODO possibly eliminate this
public interface MappingsTask extends Task {
    // TODO move this to a separate interface
    default DownloadImmediate.Builder startDownload() {
        return new DownloadImmediate.Builder(this);
    }

    // TODO add explanations to calls, probably inline method
    default void outputsNeverUpToDate() {
        this.getOutputs().upToDateWhen(task -> false);
    }
}
