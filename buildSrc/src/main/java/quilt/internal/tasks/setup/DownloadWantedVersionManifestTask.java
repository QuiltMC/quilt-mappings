package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.quiltmc.launchermeta.version_manifest.VersionEntry;
import org.quiltmc.launchermeta.version_manifest.VersionManifest;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public abstract class DownloadWantedVersionManifestTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadWantedVersionManifest";

    @InputFile
    public abstract RegularFileProperty getManifest();

    @Internal("Fingerprinting is handled by getManifest and getReleaseTime")
    protected abstract Property<VersionEntry> getManifestVersion();

    @Input
    @org.gradle.api.tasks.Optional
    protected abstract Property<String> getReleaseTime();

    // TODO make WantedVersionConsumingTask interface for this
    @OutputFile
    public abstract RegularFileProperty getVersionFile();

    public DownloadWantedVersionManifestTask() {
        super(Constants.Groups.SETUP);

        // provide an informative error message if version data can't be obtained
        this.getManifestVersion().convention(this.getProject().provider(() -> {
            throw new GradleException("No version data for Minecraft version " + Constants.MINECRAFT_VERSION);
        }));

        this.getManifestVersion().set(
            this.getManifest()
                .map(RegularFile::getAsFile)
                .map(DownloadWantedVersionManifestTask::getManifestVersion)
                .map(version -> version.orElse(null))
        );

        // have to grab the release time as there's a current timestamp on each element?!
        // TODO I don't think this is necessary, the fact that getManifest is an input should take care of it.
        this.getReleaseTime().convention(this.getManifestVersion().map(VersionEntry::getReleaseTime));
    }

    @TaskAction
    public void downloadWantedVersionManifestTask() throws IOException, URISyntaxException {
        //nb need to re-read here in case it didn't exist before
        final File versionFile = this.getVersionFile().get().getAsFile();

        FileUtils.copyURLToFile(new URI(this.getManifestVersion().get().getUrl()).toURL(), versionFile);
    }

    private static Optional<VersionEntry> getManifestVersion(File manifestFile) {
        final VersionManifest manifest;

        try {
            manifest = manifestFile.exists()
                ? VersionManifest.fromReader(Files.newBufferedReader(manifestFile.toPath(), Charset.defaultCharset()))
                : null;
        } catch (IOException e) {
            throw new GradleException("Failed to read manifest", e);
        }

        return manifest == null
            ? Optional.empty()
            : manifest.getVersions().stream().filter(it -> it.getId().equals(Constants.MINECRAFT_VERSION)).findFirst();
    }
}
