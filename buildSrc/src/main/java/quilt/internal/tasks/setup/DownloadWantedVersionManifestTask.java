package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.quiltmc.launchermeta.version_manifest.VersionEntry;
import org.quiltmc.launchermeta.version_manifest.VersionManifest;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public abstract class DownloadWantedVersionManifestTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadWantedVersionManifest";

    // TODO manifestVersion should probably be an input mapped from manifestFile
    private final File manifestFile;
    private final Optional<VersionEntry> manifestVersion;

    @OutputFile
    public abstract RegularFileProperty getVersionFile();

    public DownloadWantedVersionManifestTask() throws IOException {
        super(Constants.Groups.SETUP_GROUP);
        this.dependsOn(DownloadVersionsManifestTask.TASK_NAME);
        this.manifestFile = this.getTaskNamed(DownloadVersionsManifestTask.TASK_NAME, DownloadVersionsManifestTask.class)
            .getManifestFile().get().getAsFile();
        this.manifestVersion = getManifestVersion(this.manifestFile);

        this.getInputs().property("versionsManifest", this.manifestFile);
        // have to grab the release time as there's a current timestamp on each element?!
        this.getInputs().property(
            "releaseTime",
            this.manifestVersion.isPresent()
                ? this.manifestVersion.get().getReleaseTime()
                : -1
        );
        this.getVersionFile().convention(() ->
            new File(this.fileConstants.cacheFilesMinecraft, Constants.MINECRAFT_VERSION + ".json")
        );
    }

    @TaskAction
    public void downloadWantedVersionManifestTask() throws IOException, URISyntaxException {
        final Optional<VersionEntry> _manifestVersion = this.manifestVersion.isEmpty()
            ? getManifestVersion(this.manifestFile)
            : this.manifestVersion;
        //nb need to re-read here in case it didn't exist before
        final File versionFile = this.getVersionFile().get().getAsFile();
        if (_manifestVersion.isPresent() || versionFile.exists()) {
            if (_manifestVersion.isPresent()) {
                FileUtils.copyURLToFile(new URI(_manifestVersion.get().getUrl()).toURL(), versionFile);
            }
        } else {
            throw new RuntimeException("No version data for Minecraft version " + Constants.MINECRAFT_VERSION);
        }
    }

    private static Optional<VersionEntry> getManifestVersion(File manifestFile) throws IOException {
        final VersionManifest manifest = manifestFile.exists()
            ? VersionManifest.fromReader(Files.newBufferedReader(manifestFile.toPath(), Charset.defaultCharset()))
            : null;
        return manifest == null
            ? Optional.empty()
            : manifest.getVersions().stream().filter(it -> it.getId().equals(Constants.MINECRAFT_VERSION)).findFirst();
    }
}
