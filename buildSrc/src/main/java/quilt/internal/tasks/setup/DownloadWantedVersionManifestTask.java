package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import groovy.json.JsonSlurper;
import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.units.qual.C;
import org.gradle.api.DefaultTask;
import quilt.internal.Constants;
import quilt.internal.MappingsPlugin;

public class DownloadWantedVersionManifestTask extends DefaultTask {
    public DownloadWantedVersionManifestTask() throws IOException {
        this.setGroup(Constants.Groups.SETUP_GROUP);
        this.dependsOn("downloadVersionsManifest");
        DownloadVersionsManifestTask downloadVersionsManifest = (DownloadVersionsManifestTask) getProject().getTasks().getByName("downloadVersionsManifest");
        File manifestFile = downloadVersionsManifest.getOutputs().getFiles().getSingleFile();
        Optional<Object> manifestVersion = getManifestVersion(manifestFile);

        //have to grab the release time as there's a current timestamp on each element?!
        getInputs().property("releaseTime", manifestVersion.isPresent() ? ((Map<String, ?>) manifestVersion.get()).get("releaseTime") : -1);

        getOutputs().file(MappingsPlugin.getExtension(getProject()).getFileConstants().versionFile);

        doLast(_this -> {
            try {
                Optional<Object> _manifestVersion = manifestVersion.isEmpty() ? getManifestVersion(manifestFile) : manifestVersion;
                //nb need to re-read here in case it didn't exist before
                if (_manifestVersion.isPresent() || MappingsPlugin.getExtension(getProject()).getFileConstants().versionFile.exists()) {

                    if (_manifestVersion.isPresent()) {
                        FileUtils.copyURLToFile(new URL(((String) ((Map<String, ?>) _manifestVersion.get()).get("url"))), MappingsPlugin.getExtension(getProject()).getFileConstants().versionFile);
                    }
                } else {
                    throw new RuntimeException("No version data for Minecraft version " + Constants.MINECRAFT_VERSION);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static Optional<Object> getManifestVersion(File manifestFile) throws IOException {
        Map<String, ?> manifest = manifestFile.exists() ? (Map<String, ?>) new JsonSlurper().parseText(FileUtils.readFileToString(manifestFile)) : null;
        return manifest != null ? ((List<Object>) manifest.get("versions")).stream().filter(it -> (((Map<String, ?>) it).get("id")).equals(Constants.MINECRAFT_VERSION)).findFirst() : Optional.empty();
    }
}
