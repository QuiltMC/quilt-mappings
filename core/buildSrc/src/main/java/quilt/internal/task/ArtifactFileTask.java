package quilt.internal.task;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.maven.MavenArtifact;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;

import javax.inject.Inject;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static quilt.internal.util.ProviderUtil.toOptional;

/**
 * A task that produces an {@link #getArtifactFile() artifactFile}.
 * <p>
 * The path to the {@link #getArtifactFile() artifactFile} is built from the task's name and destination properties.<br>
 * Rather than passing the {@link #getArtifactFile() artifactFile} to {@link MavenPublication#artifact(Object)}, use
 * one of the {@link #artifact} helper methods to publish it to a maven.
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
     * <i>Don't</i> pass this to {@link MavenPublication#artifact(Object)},
     * use one the task's {@link #artifact} helper methods instead.
     */
    @OutputFile
    default Provider<RegularFile> getArtifactFile() {
        // zzzzzip
        return this.getArtifactBaseName()
            .zip(this.getArtifactAppendix().orElse(""), ArtifactFileTask::dashJoin)
            .zip(this.getArtifactVersion().orElse(""), ArtifactFileTask::dashJoin)
            .zip(this.getArtifactClassifier().orElse(""), ArtifactFileTask::dashJoin)
            .zip(this.getArtifactExtension(), ArtifactFileTask::dotJoin)
            .zip(this.getDestinationDirectory(), (name, dest) -> dest.file(name));
    }

    /**
     * Adds an {@linkplain MavenArtifact artifact} to the passed {@code publication} that:
     * <ul>
     *     <li> contains this tasks' {@linkplain #getArtifactFile() artifact}
     *     <li> has this tasks' {@linkplain #getArtifactClassifier() classifier}
     *     <li> is {@link MavenArtifact#builtBy(Object...) builtBy} this task
     * </ul>
     * Build script usage:
     * <pre>
     *     {@code
     *          publishing {
     *            publications {
     *              maven(MavenPublication) {
     *                exampleArtifactFileTask.artifact maven
     *              }
     *            }
     *          }
     *      }
     * </pre>
     *
     * @return the added artifact
     */
    default MavenArtifact artifact(MavenPublication publication) {
        return publication.artifact(this.getArtifactFile(), artifact -> {
            artifact.builtBy(this);
            toOptional(this.getArtifactClassifier()).ifPresent(artifact::setClassifier);
        });
    }

    /**
     * Build script usage:
     * <pre>
     *     {@code
     *          publishing {
     *            publications {
     *              maven(MavenPublication) {
     *                exampleArtifactFileTask.artifact maven {
     *                  classifier 'example-adhoc-classifier'
     *                }
     *              }
     *            }
     *          }
     *      }
     * </pre>
     *
     * @see #artifact(MavenPublication)
     */
    default MavenArtifact artifact(MavenPublication publication, Action<? super MavenArtifact> configuration) {
        final MavenArtifact artifact = this.artifact(publication);

        configuration.execute(artifact);

        return artifact;
    }

    private static String dashJoin(String left, String right) {
        return joinNonEmpty("-", left, right);
    }

    private static String dotJoin(String left, String right) {
        return joinNonEmpty(".", left, right);
    }

    private static String joinNonEmpty(String separator, String... strings) {
        return Stream.of(strings)
            .filter(string -> !string.isEmpty())
            .collect(Collectors.joining(separator));
    }
}
