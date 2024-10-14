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

import java.io.File;
import java.nio.file.Path;

/**
 * A task that takes a target version as input.
 * <p>
 * A target version is a published Quilt Mappings version obtained from the Quilt maven.
 * <p>
 * If {@link QuiltMappingsPlugin MappingsPlugin} is applied, any {@code TargetVersionConsumingTask}s
 * will use {@value CheckTargetVersionExistsTask#TASK_NAME}'s
 * {@link CheckTargetVersionExistsTask#getTargetVersion() targetVersion} by default, and they'll only run if the
 * {@link #getTargetVersion() targetVersion} {@link Provider#isPresent() isPresent}.
 */
public interface TargetVersionConsumingTask extends MappingsTask {
    @Input
    @Optional
    Property<String> getTargetVersion();

    /**
     * @param pathFactory receives the {@linkplain #getTargetVersion() target version}
     *                   and returns the path of the file to be provided; the path must represent a {@link RegularFile},
     *                   and relative paths will be resolved against the
     *                   {@linkplain org.gradle.api.file.ProjectLayout#getProjectDirectory() project directory}
     */
    default Provider<RegularFile> provideVersionedProjectFile(Transformer<Path, String> pathFactory) {
        return this.getProject().getLayout().file(this.providerVersionedFile(pathFactory));
    }

    /**
     * @param pathFactory receives the {@linkplain #getTargetVersion() target version}
     *                   and returns the path of the file to be provided; the path must represent a {@link Directory},
     *                   and relative paths will be resolved against the
     *                   {@linkplain org.gradle.api.file.ProjectLayout#getProjectDirectory() project directory}
     */
    default Provider<Directory> provideVersionedProjectDir(Transformer<Path, String> pathFactory) {
        return this.getProject().getLayout().dir(this.providerVersionedFile(pathFactory));
    }

    /**
     * @param pathFactory receives the {@linkplain #getTargetVersion() target version}
     *                   and returns the path of the file to be provided
     */
    default Provider<File> providerVersionedFile(Transformer<Path, String> pathFactory) {
        return this.getTargetVersion().map(pathFactory).map(Path::toFile);
    }
}
