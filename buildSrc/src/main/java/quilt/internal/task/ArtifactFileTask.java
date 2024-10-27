package quilt.internal.task;

import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;

import javax.inject.Inject;

// TODO would it be screwy to implement PublishArtifact on this?
//  It would allow passing the task itself to MavenPublication#artifact, instead of artifactFile.

/**
 * A task that produces an {@link #getArtifactFile() artifactFile}.
 * <p>
 * The path to the {@link #getArtifactFile() artifactFile} is built from the task's name and destination properties,
 * and {@link MavenPublication#artifact(Object)} can interpolate artifact metadata from the name's format.
 */
public interface ArtifactFileTask extends Task {
    @Inject
    ObjectFactory getObjects();

    @Input
    Property<String> getArtifactBaseName();

    @Optional
    @Input
    Property<String> getArtifactAppendix();

    @Optional
    @Input
    Property<String> getArtifactVersion();

    @Optional
    @Input
    Property<String> getArtifactClassifier();

    @Input
    Property<String> getArtifactExtension();

    /**
     * Required
     */
    @Internal("Represented as part of artifactFile")
    DirectoryProperty getDestinationDirectory();

    /**
     * The artifact produced by this task.
     * <p>
     * The path to the file takes the form:<br>
     * {@code [destination]/[baseName]-[appendix]-[version]-[classifier].[extension]}
     * <p>
     * This standard format allows {@link MavenPublication#artifact(Object)} to interpolate the
     * {@linkplain #getArtifactClassifier() classifier} and the {@linkplain #getArtifactExtension() extension}.
     */
    @OutputFile
    default Provider<RegularFile> getArtifactFile() {
        // zzzzzip
        return this.getArtifactBaseName()
            .zip(this.getArtifactAppendix().orElse(""), ArtifactFileTask::dashJoin)
            .zip(this.getArtifactVersion().orElse(""), ArtifactFileTask::dashJoin)
            .zip(this.getArtifactClassifier().orElse(""), ArtifactFileTask::dashJoin)
            .zip(this.getArtifactExtension(), (name, ext) -> name + "." + ext)
            .zip(this.getDestinationDirectory(), (name, dest) -> dest.file(name));
    }

    private static String dashJoin(String left, String right) {
        return right.isEmpty()
            ? left
            : left + "-" + right;
    }
}
