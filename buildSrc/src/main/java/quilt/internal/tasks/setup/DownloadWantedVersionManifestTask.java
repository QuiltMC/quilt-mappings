package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.util.json.ManifestFile;

public class DownloadWantedVersionManifestTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadWantedVersionManifest";

    private final File manifestFile;
    private final Optional<ManifestFile.Version> manifestVersion;

    @OutputFile
    private File versionFile;

    public DownloadWantedVersionManifestTask() throws IOException {
        super(Constants.Groups.SETUP_GROUP);
        this.dependsOn(DownloadVersionsManifestTask.TASK_NAME);
        manifestFile = this.<DownloadVersionsManifestTask>getTaskByName(DownloadVersionsManifestTask.TASK_NAME).getManifestFile();
        manifestVersion = getManifestVersion(manifestFile);

        //have to grab the release time as there's a current timestamp on each element?!
        getInputs().property("releaseTime", manifestVersion.isPresent() ? manifestVersion.get().releaseTime() : -1);
        versionFile = new File(fileConstants.cacheFilesMinecraft, Constants.MINECRAFT_VERSION + ".json");
    }

    @TaskAction
    public void downloadWantedVersionManifestTask() throws IOException {
        Optional<ManifestFile.Version> _manifestVersion = manifestVersion.isEmpty() ? getManifestVersion(manifestFile) : manifestVersion;
        //nb need to re-read here in case it didn't exist before
        if (_manifestVersion.isPresent() || versionFile.exists()) {
            if (_manifestVersion.isPresent()) {
                FileUtils.copyURLToFile(new URL((_manifestVersion.get().url())), versionFile);
            }
        } else {
            throw new RuntimeException("No version data for Minecraft version " + Constants.MINECRAFT_VERSION);
        }
    }

    private static Optional<ManifestFile.Version> getManifestVersion(File manifestFile) throws IOException {
        ManifestFile manifest = manifestFile.exists() ? ManifestFile.fromJson(FileUtils.readFileToString(manifestFile, Charset.defaultCharset())) : null;
        return manifest != null ? manifest.versions().stream().filter(it -> it.id().equals(Constants.MINECRAFT_VERSION)).findFirst() : Optional.empty();
    }

    public File getVersionFile() {
        return versionFile;
    }
}
