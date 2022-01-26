package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.quiltmc.launchermeta.version_manifest.VersionEntry;
import org.quiltmc.launchermeta.version_manifest.VersionManifest;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public class DownloadWantedVersionManifestTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadWantedVersionManifest";

    private final File manifestFile;
    private final Optional<VersionEntry> manifestVersion;

    @OutputFile
    private final File versionFile;

    public DownloadWantedVersionManifestTask() throws IOException {
        super(Constants.Groups.SETUP_GROUP);
        this.dependsOn(DownloadVersionsManifestTask.TASK_NAME);
        manifestFile = this.<DownloadVersionsManifestTask>getTaskByName(DownloadVersionsManifestTask.TASK_NAME).getManifestFile();
        manifestVersion = getManifestVersion(manifestFile);

        getInputs().property("versionsManifest", manifestFile);
        //have to grab the release time as there's a current timestamp on each element?!
        getInputs().property("releaseTime", manifestVersion.isPresent() ? manifestVersion.get().getReleaseTime() : -1);
        versionFile = new File(fileConstants.cacheFilesMinecraft, Constants.MINECRAFT_VERSION + ".json");
    }

    @TaskAction
    public void downloadWantedVersionManifestTask() throws IOException {
        Optional<VersionEntry> _manifestVersion = manifestVersion.isEmpty() ? getManifestVersion(manifestFile) : manifestVersion;
        //nb need to re-read here in case it didn't exist before
        if (_manifestVersion.isPresent() || versionFile.exists()) {
            if (_manifestVersion.isPresent()) {
                FileUtils.copyURLToFile(new URL((_manifestVersion.get().getUrl())), versionFile);
            }
        } else {
            throw new RuntimeException("No version data for Minecraft version " + Constants.MINECRAFT_VERSION);
        }
    }

    private static Optional<VersionEntry> getManifestVersion(File manifestFile) throws IOException {
        VersionManifest manifest = manifestFile.exists() ? VersionManifest.fromReader(Files.newBufferedReader(manifestFile.toPath(), Charset.defaultCharset())) : null;
        return manifest != null ? manifest.getVersions().stream().filter(it -> it.getId().equals(Constants.MINECRAFT_VERSION)).findFirst() : Optional.empty();
    }

    public File getVersionFile() {
        return versionFile;
    }
}
