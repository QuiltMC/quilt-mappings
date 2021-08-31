package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.MappingsPlugin;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.util.JsonUtils;

public class DownloadWantedVersionManifestTask extends DefaultMappingsTask {
    private final File manifestFile;
    private final Optional<Map<String, String>> manifestVersion;

    public DownloadWantedVersionManifestTask() throws IOException {
        super(Constants.Groups.SETUP_GROUP);
        this.dependsOn("downloadVersionsManifest");
        DownloadVersionsManifestTask downloadVersionsManifest = getTaskFromName("downloadVersionsManifest");
        manifestFile = downloadVersionsManifest.getOutputs().getFiles().getSingleFile();
        manifestVersion = getManifestVersion(manifestFile);

        //have to grab the release time as there's a current timestamp on each element?!
        getInputs().property("releaseTime", manifestVersion.isPresent() ? manifestVersion.get().get("releaseTime") : -1);

        getOutputs().file(MappingsPlugin.getExtension(getProject()).getFileConstants().versionFile);

    }

    @TaskAction
    public void downloadWantedVersionManifestTask() throws IOException {
        Optional<Map<String, String>> _manifestVersion = manifestVersion.isEmpty() ? getManifestVersion(manifestFile) : manifestVersion;
        //nb need to re-read here in case it didn't exist before
        if (_manifestVersion.isPresent() || MappingsPlugin.getExtension(getProject()).getFileConstants().versionFile.exists()) {

            if (_manifestVersion.isPresent()) {
                FileUtils.copyURLToFile(new URL((_manifestVersion.get().get("url"))), MappingsPlugin.getExtension(getProject()).getFileConstants().versionFile);
            }
        } else {
            throw new RuntimeException("No version data for Minecraft version " + Constants.MINECRAFT_VERSION);
        }
    }

    private static Optional<Map<String, String>> getManifestVersion(File manifestFile) throws IOException {
        Map<String, List<Map<String, String>>> manifest = manifestFile.exists() ? JsonUtils.getFromTree(FileUtils.readFileToString(manifestFile, Charset.defaultCharset())) : null;
        return manifest != null ? manifest.get("versions").stream().filter(it -> it.get("id").equals(Constants.MINECRAFT_VERSION)).findFirst() : Optional.empty();
    }
}
