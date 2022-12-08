package quilt.internal.tasks;

import org.gradle.api.Task;
import org.gradle.api.artifacts.VersionCatalog;
import org.gradle.api.artifacts.VersionCatalogsExtension;
import quilt.internal.MappingsExtension;
import quilt.internal.MappingsPlugin;
import quilt.internal.util.DownloadImmediate;

public interface MappingsTask extends Task {
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

    default MappingsExtension mappingsExt() {
        return MappingsPlugin.getExtension(getProject());
    }

    default VersionCatalogsExtension versionCatalogs() {
        return getProject().getExtensions().getByType(VersionCatalogsExtension.class);
    }

    default VersionCatalog libs() {
        return versionCatalogs().named("libs");
    }
}
