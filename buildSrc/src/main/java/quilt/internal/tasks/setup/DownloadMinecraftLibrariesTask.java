package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.quiltmc.launchermeta.version.v1.DownloadableFile;
import org.quiltmc.launchermeta.version.v1.Version;
import quilt.internal.Constants;
import quilt.internal.FileConstants;
import quilt.internal.tasks.DefaultMappingsTask;

public abstract class DownloadMinecraftLibrariesTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadMinecraftLibraries";

    @InputFile
    public abstract RegularFileProperty getVersionFile();

    public DownloadMinecraftLibrariesTask() {
        super(Constants.Groups.SETUP_GROUP);

        this.dependsOn(DownloadWantedVersionManifestTask.TASK_NAME);

        this.getVersionFile().convention(
            this.getTaskNamed(DownloadWantedVersionManifestTask.TASK_NAME, DownloadWantedVersionManifestTask.class)
                .getVersionFile()
        );

        this.getOutputs().dir(this.fileConstants.libraries);
        this.outputsNeverUpToDate();
    }

    @TaskAction
    public void downloadMinecraftLibrariesTask() throws IOException {
        final Version file = Version.fromString(FileUtils.readFileToString(
            this.getVersionFile().get().getAsFile(), StandardCharsets.UTF_8
        ));

        this.getLogger().lifecycle(":downloading minecraft libraries");

        if (!this.fileConstants.libraries.exists()) {
            this.fileConstants.libraries.mkdirs();
        }

        final AtomicBoolean failed = new AtomicBoolean(false);

        final Object lock = new Object();

        file.getLibraries().parallelStream().forEach(library -> {
            final Optional<DownloadableFile.PathDownload> artifact = library.getDownloads().getArtifact();
            if (artifact.isEmpty()) {
                return;
            }

            try {
                final String url = artifact.get().getUrl();
                this.startDownload()
                        .src(url)
                        .dest(getArtifactFile(this.fileConstants, url))
                        .overwrite(false)
                        .download();
            } catch (IOException e) {
                failed.set(true);
                e.printStackTrace();
            } catch (NoSuchElementException e) {
                new RuntimeException("Unable to find artifact for " + library.getName(), e).printStackTrace();
            }

            synchronized (lock) {
                this.getProject().getDependencies().add("decompileClasspath", library.getName());
            }
        });

        if (failed.get()) {
            throw new RuntimeException("Unable to download libraries for specified minecraft version.");
        }
    }

    public static File getArtifactFile(FileConstants fileConstants, String url) {
        return new File(fileConstants.libraries, url.substring(url.lastIndexOf("/") + 1));
    }
}
