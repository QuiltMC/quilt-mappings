package quilt.internal.tasks;

import org.gradle.api.Task;
import org.gradle.api.artifacts.VersionCatalog;
import org.gradle.api.artifacts.VersionCatalogsExtension;
import quilt.internal.QuiltMappingsExtension;
import quilt.internal.util.DownloadImmediate;

// TODO check if tasks that depend on other other tasks' outputs have task dependencies that can be eliminated
public interface MappingsTask extends Task {
    default DownloadImmediate.Builder startDownload() {
        return new DownloadImmediate.Builder(this);
    }

    // TODO eliminate this
    default <T extends Task> T getTaskNamed(String name, Class<T> taskClass) {
        return this.getProject().getTasks().named(name, taskClass).get();
    }

    // TODO add explanations to calls
    default void outputsNeverUpToDate() {
        this.getOutputs().upToDateWhen(task -> false);
    }

    default QuiltMappingsExtension mappingsExt() {
        return QuiltMappingsExtension.get(this.getProject());
    }

    default VersionCatalogsExtension versionCatalogs() {
        return this.getProject().getExtensions().getByType(VersionCatalogsExtension.class);
    }

    default VersionCatalog libs() {
        return this.versionCatalogs().named("libs");
    }
}
