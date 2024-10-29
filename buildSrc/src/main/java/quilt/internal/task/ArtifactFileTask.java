package quilt.internal.task;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.maven.MavenArtifact;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;

// TODO Is there a way to make it so MavenPublication#artifact will accept
//  these tasks directly and run them to build their outputs, similar to AbstractArchiveTask?
//  (eliminating the need for the artifact convenience methods)
//  I considered implementing PublishArtifact on this, but it's annotated with @HasInternalProtocol.
//  This suggests that it would work but doesn't recommend it:
//  https://github.com/gradle/gradle/issues/17273#issuecomment-858400396

/**
 * A task that produces an {@link #getArtifactFile() artifactFile}.
 * <p>
 * The path to the {@link #getArtifactFile() artifactFile} is built from the task's name and destination properties,
 * and {@link MavenPublication#artifact(Object)} can interpolate artifact metadata from the name's format.
 */
public interface ArtifactFileTask extends Task {
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

    /**
     * Convenient hack to provide this task as an artifact source.
     * <p>
     * If this task's {@link #getArtifactFile() artifactFile} is its only output,
     * {@link MavenPublication#artifact(Object)} can retrieve it from this method's
     * provider and automatically run this task to build it.
     * <p>
     * <b>Accesses {@link #getProject() project}: do not use during task execution</b>.
     * <p>
     * Build script usage:
     * <pre>
     *     {@code
     *          publishing {
     *            publications {
     *              maven(MavenPublication) {
     *                artifact exampleArtifactFileTask.artifact
     *              }
     *            }
     *          }
     *      }
     * </pre>
     *
     * @return a provider of this task
     */
    @Internal("not an input or an output")
    default Provider<ArtifactFileTask> getArtifact() {
        // can't use a Provider from a ProviderFactory, I think it has to ba a TaskProvider
        return this.getProject().getTasks().named(this.getName(), ArtifactFileTask.class);
    }

    /**
     * Add an {@linkplain MavenArtifact artifact} to the passed {@code publication} consisting of this task's
     * {@link #getArtifactFile() artifactFile} and {@link MavenArtifact#builtBy(Object...) builtBy} this task.
     * <p>
     * Prefer {@link #getArtifact() artifact} for tasks whose only output is their
     * {@link #getArtifactFile() artifactFile}.
     * <p>
     * Build script usage:
     * <pre>
     *     {@code
     *          publishing {
     *            publications {
     *              maven(MavenPublication) {
     *                exampleArtifactFileTask.artifact(maven)
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
        });
    }

    /**
     * Build script usage:
     * <pre>
     *     {@code
     *          publishing {
     *            publications {
     *              maven(MavenPublication) {
     *                exampleArtifactFileTask.artifact(maven) {
     *                  classifier 'example-classifier'
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
        return right.isEmpty()
            ? left
            : left + "-" + right;
    }
}
