package quilt.internal.tasks;

import org.gradle.api.Task;
import quilt.internal.util.DownloadImmediate;

public interface AbstractMappingsTask extends Task {
    default DownloadImmediate.Builder startDownload() {
        return new DownloadImmediate.Builder(this);
    }

    @SuppressWarnings("unchecked")
    default <T extends Task> T getTaskByName(String taskName) {
        return (T) getProject().getTasks().getByName(taskName);
    }

    default <T extends Task> T getTaskByType(Class<T> taskClass) {
        return getProject().getTasks().stream().filter(task -> taskClass.isAssignableFrom(task.getClass())).map(taskClass::cast).findAny().orElseThrow();
    }

    default void outputsNeverUpToDate() {
        this.getOutputs().upToDateWhen(task -> false);
    }
}
