package quilt.internal.tasks;

import org.gradle.api.Task;
import quilt.internal.QuiltMappingsExtension;
import quilt.internal.util.DownloadImmediate;

// TODO possibly eliminate this
public interface MappingsTask extends Task {
    // TODO move this to a separate interface
    default DownloadImmediate.Builder startDownload() {
        return new DownloadImmediate.Builder(this);
    }

    // TODO eliminate this
    default <T extends Task> T getTaskNamed(String name, Class<T> taskClass) {
        return this.getProject().getTasks().named(name, taskClass).get();
    }

    // TODO add explanations to calls, probably inline method
    default void outputsNeverUpToDate() {
        this.getOutputs().upToDateWhen(task -> false);
    }

    // TODO eliminate this
    default QuiltMappingsExtension mappingsExt() {
        return QuiltMappingsExtension.get(this.getProject());
    }
}
