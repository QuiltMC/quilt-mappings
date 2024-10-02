package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.quiltmc.launchermeta.version.v1.DownloadableFile;
import org.quiltmc.launchermeta.version.v1.Library;
import org.quiltmc.launchermeta.version.v1.Version;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

// TODO see if this can be replaced with a ValueSource or a BuildService
public abstract class DownloadMinecraftLibrariesTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadMinecraftLibraries";

    @InputFile
    public abstract RegularFileProperty getVersionFile();

    // TODO put this in something Serializable or make this Transient
    @Internal("Fingerprinting is handled by getVersionFile")
    protected abstract Property<Version> getVersion();

    @OutputDirectory
    public abstract DirectoryProperty getLibrariesDir();

    @Internal("Fingerprinting is handled by getLibrariesDir")
    protected abstract MapProperty<String, File> getArtifactsByUrlImpl();

    public DownloadMinecraftLibrariesTask() {
        super(Constants.Groups.SETUP_GROUP);
        // TODO is this because library sources may change even on the same version?
        this.outputsNeverUpToDate();

        this.getVersion().convention(this.getVersionFile().map(file -> {
            try {
                return Version.fromString(FileUtils.readFileToString(file.getAsFile(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new GradleException("Failed to read version file", e);
            }
        }));

        // provide an informative error message if this property is accessed incorrectly
        this.getArtifactsByUrlImpl().convention(this.getProject().provider(() -> {
            throw new GradleException(
                "artifactsByUrl has not been populated. " +
                    "It should only be accessed from other tasks' actions and via lazy input."
            );
        }));
    }

    @TaskAction
    public void downloadMinecraftLibrariesTask() {
        this.getLogger().lifecycle(":downloading minecraft libraries");

        this.getLibrariesDir().get().getAsFile().mkdirs();

        final AtomicBoolean failed = new AtomicBoolean(false);

        final Object lock = new Object();

        final List<Library> librariesSrc = this.getVersion().get().getLibraries();

        final Map<String, File> artifactsByUrl = librariesSrc.parallelStream().flatMap(library -> {
            final Optional<DownloadableFile.PathDownload> artifactPath = library.getDownloads().getArtifact();
            if (artifactPath.isEmpty()) {
                return Stream.empty();
            }

            final String url = artifactPath.get().getUrl();
            final File artifact = this.artifactOf(url);

            boolean thisFailed = false;
            try {
                this.startDownload()
                    .src(url)
                    .dest(artifact)
                    .overwrite(false)
                    .download();
            } catch (IOException e) {
                thisFailed = true;
                e.printStackTrace();
            } catch (NoSuchElementException e) {
                new RuntimeException("Unable to find artifact for " + library.getName(), e).printStackTrace();
            }

            synchronized (lock) {
                // TODO this is screwy.
                //  Could we put these in a configuration in MappingsPlugin
                //  and pass the configuration to an input of this task?
                this.getProject().getDependencies().add("decompileClasspath", library.getName());
            }

            if (thisFailed) {
                failed.set(true);

                return Stream.empty();
            } else {
                return Stream.of(Map.entry(url, artifact));
            }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // TODO is it intended that downloading continues after the first failure,
        //  but we still throw an exception after we've tried downloading each library?
        if (failed.get()) {
            throw new RuntimeException("Unable to download libraries for specified minecraft version.");
        }

        this.getArtifactsByUrlImpl().set(artifactsByUrl);
    }

    // TODO can this instead put artifacts in sub-dirs based on urls, so consumers can take getLibrariesDir as input
    //  and retrieve by url by finding the right sub-dir?
    /**
     * This is only populated after the task has run.
     * <p>
     * It should only be accessed from other tasks' {@linkplain  TaskAction actions} and via
     * {@linkplain MapProperty lazy} {@linkplain org.gradle.api.tasks.Input input}.
     */
    @Internal
    public Provider<Map<String, File>> getArtifactsByUrl() {
        return this.getArtifactsByUrlImpl();
    }

    private File artifactOf(String url) {
        return new File(
            this.getLibrariesDir().get().getAsFile(),
            url.substring(url.lastIndexOf("/") + 1)
        );
    }
}
