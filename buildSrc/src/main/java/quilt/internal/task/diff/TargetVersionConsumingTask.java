package quilt.internal.task.diff;

import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import quilt.internal.plugin.TargetDiffPlugin;
import quilt.internal.util.Version;

/**
 * A task that takes a target version as input.
 * <p>
 * A target version is a published Quilt Mappings version obtained from the Quilt maven.
 *
 * @see TargetDiffPlugin TargetDiffPlugin's configureEach
 */
public interface TargetVersionConsumingTask extends Task {
    @Input
    @Optional
    Property<Version> getTargetVersion();

    default Provider<String> provideTargetVersionString() {
        return this.getTargetVersion().map(Version::string);
    }

    /**
     * @param destinationDir the {@link Directory} the provided file will be resolved against
     * @param namer receives the {@link #getTargetVersion() targetVersion}
     *             and returns the name of the file to be provided
     */
    default Provider<RegularFile> provideVersionedFile(Directory destinationDir, Transformer<String, String> namer) {
        return this.provideTargetVersionString().map(namer).map(destinationDir::file);
    }

    /**
     * @param destinationDir the {@link Directory} the provided file will be resolved against
     * @param namer receives the {@link #getTargetVersion() targetVersion}
     *             and returns the name of the file to be provided
     */
    default Provider<RegularFile> provideVersionedFile(
        Provider<Directory> destinationDir, Transformer<String, String> namer
    ) {
        return destinationDir.zip(this.provideTargetVersionString().map(namer), Directory::file);
    }

    /**
     * @param destinationDir the {@link Directory} the provided directory will be resolved against
     * @param namer receives the {@link #getTargetVersion() targetVersion}
     *             and returns the name of the directory to be provided
     */
    default Provider<Directory> provideVersionedDir(Directory destinationDir, Transformer<String, String> namer) {
        return this.provideTargetVersionString().map(destinationDir::dir);
    }

    /**
     * @param destinationDir the {@link Directory} the provided directory will be resolved against
     * @param namer receives the {@link #getTargetVersion() targetVersion}
     *             and returns the name of the directory to be provided
     */
    default Provider<Directory> provideVersionedDir(
        Provider<Directory> destinationDir, Transformer<String, String> namer
    ) {
        return destinationDir.zip(this.provideTargetVersionString(), Directory::dir);
    }
}
