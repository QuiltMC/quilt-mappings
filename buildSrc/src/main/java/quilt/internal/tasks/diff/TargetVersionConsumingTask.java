package quilt.internal.tasks.diff;

import org.gradle.api.Transformer;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import quilt.internal.QuiltMappingsPlugin;
import quilt.internal.tasks.MappingsTask;

/**
 * A task that takes a target version as input.
 * <p>
 * A target version is a published Quilt Mappings version obtained from the Quilt maven.
 * <p>
 * If {@link QuiltMappingsPlugin MappingsPlugin} is applied, any {@code TargetVersionConsumingTask}s
 * will use the {@linkplain DownloadTargetMetaFileTask#provideTargetVersion() target version}
 * {@value DownloadTargetMetaFileTask#TASK_NAME} provides by default, and they'll only run if their
 * {@link #getTargetVersion() targetVersion} {@link Provider#isPresent() isPresent}.
 */
public interface TargetVersionConsumingTask extends MappingsTask {
    @Input
    @Optional
    Property<String> getTargetVersion();

    /**
     * @param destinationDir the {@link Directory} the provided file will be resolved against
     * @param namer receives the {@link #getTargetVersion() targetVersion}
     *             and returns the name of the file to be provided
     */
    default Provider<RegularFile> provideVersionedFile(Directory destinationDir, Transformer<String, String> namer) {
        return this.getTargetVersion().map(namer).map(destinationDir::file);
    }

    /**
     * @param destinationDir the {@link Directory} the provided directory will be resolved against
     * @param namer receives the {@link #getTargetVersion() targetVersion}
     *             and returns the name of the directory to be provided
     */
    default Provider<Directory> provideVersionedDir(Directory destinationDir, Transformer<String, String> namer) {
        return this.getTargetVersion().map(namer).map(destinationDir::dir);
    }
}
