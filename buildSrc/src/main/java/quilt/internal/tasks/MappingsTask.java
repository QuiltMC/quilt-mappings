package quilt.internal.tasks;

import org.gradle.api.Task;
import quilt.internal.util.DownloadImmediate;

public interface MappingsTask extends Task {

    default DownloadImmediate.Builder startDownload() {
        return new DownloadImmediate.Builder(this);
    }

    @SuppressWarnings("unchecked")
    default <T extends Task> T getTaskFromName(String taskName) {
        return (T) getProject().getTasks().getByName(taskName);
    }

    default void outputsNeverUpToDate() {
        this.getOutputs().upToDateWhen(task -> false);
    }
}
