package quilt.internal.tasks;

import org.gradle.api.Task;
import org.gradle.api.artifacts.VersionCatalog;
import org.gradle.api.artifacts.VersionCatalogsExtension;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import quilt.internal.QuiltMappingsExtension;
import quilt.internal.util.DownloadImmediate;

// TODO check if tasks that depend on other other tasks' outputs have task dependencies that can be eliminated
public interface MappingsTask extends Task {
    default DownloadImmediate.Builder startDownload() {
        return new DownloadImmediate.Builder(this);
    }

    default Task getTaskNamed(String taskName) {
        return this.getProject().getTasks().getByName(taskName);
    }

    default <T extends Task> T getTaskNamed(String name, Class<T> taskClass) {
        return this.getProject().getTasks().named(name, taskClass).get();
    }

    default RegularFile regularProjectFileOf(String path) {
        return this.getProjectDirectory().file(path);
    }

    default Provider<RegularFile> regularProjectFileOf(Provider<? extends CharSequence> path) {
        return this.getProjectDirectory().file(path);
    }

    private Directory getProjectDirectory() {
        return this.getProject().getLayout().getProjectDirectory();
    }

    // TODO: replace usage of this with @DisableCachingByDefault or @UntrackedTask on task classes that use it,
    //  with explanations
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
