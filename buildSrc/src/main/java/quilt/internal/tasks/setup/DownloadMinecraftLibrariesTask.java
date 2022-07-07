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
import quilt.internal.tasks.DefaultMappingsTask;

public class DownloadMinecraftLibrariesTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadMinecraftLibraries";

    @InputFile
    private final RegularFileProperty versionFile;

    public DownloadMinecraftLibrariesTask() {
        super(Constants.Groups.SETUP_GROUP);
        dependsOn(DownloadWantedVersionManifestTask.TASK_NAME);

        versionFile = getProject().getObjects().fileProperty();
        versionFile.convention(getTaskByType(DownloadWantedVersionManifestTask.class)::getVersionFile);

        getInputs().file(versionFile);

        getOutputs().dir(fileConstants.libraries);
        outputsNeverUpToDate();
    }

    @TaskAction
    public void downloadMinecraftLibrariesTask() throws IOException {
        Version file = Version.fromString(FileUtils.readFileToString(versionFile.get().getAsFile(), StandardCharsets.UTF_8));

        getLogger().lifecycle(":downloading minecraft libraries");

        if (!fileConstants.libraries.exists()) {
            fileConstants.libraries.mkdirs();
        }

        AtomicBoolean failed = new AtomicBoolean(false);

        Object lock = new Object();

        file.getLibraries().parallelStream().forEach(library -> {
            Optional<DownloadableFile.PathDownload> artifact = library.getDownloads().getArtifact();
            if (artifact.isEmpty()) {
                return;
            }

            try {
                String url = artifact.get().getUrl();
                startDownload()
                        .src(url)
                        .dest(new File(fileConstants.libraries, url.substring(url.lastIndexOf("/") + 1)))
                        .overwrite(false)
                        .download();
            } catch (IOException e) {
                failed.set(true);
                e.printStackTrace();
            } catch (NoSuchElementException e) {
                new RuntimeException("Unable to find artifact for " + library.getName(), e).printStackTrace();
            }

            synchronized (lock) {
                getProject().getDependencies().add("decompileClasspath", library.getName());
            }
        });

        if (failed.get()) {
            throw new RuntimeException("Unable to download libraries for specified minecraft version.");
        }
    }

    public RegularFileProperty getVersionFile() {
        return versionFile;
    }
}
