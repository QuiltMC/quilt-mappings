package quilt.internal.task.diff;

import org.gradle.api.Transformer;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import quilt.internal.plugin.QuiltMappingsPlugin;
import quilt.internal.plugin.TargetDiffPlugin;
import quilt.internal.task.MappingsTask;

/**
 * A task that takes a target version as input.
 * <p>
 * A target version is a published Quilt Mappings version obtained from the Quilt maven.
 *
 * @see TargetDiffPlugin TargetDiffPlugin's configureEach
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
