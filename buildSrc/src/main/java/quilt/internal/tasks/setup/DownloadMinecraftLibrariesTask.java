package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import de.undercouch.gradle.tasks.download.DownloadAction;
import groovy.json.JsonSlurper;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import quilt.internal.Constants;
import quilt.internal.FileConstants;
import quilt.internal.MappingsPlugin;

public class DownloadMinecraftLibrariesTask extends DefaultTask {
    public DownloadMinecraftLibrariesTask() {
        dependsOn("downloadWantedVersionManifest");
        setGroup(Constants.Groups.SETUP_GROUP);
        FileConstants fileConstants = MappingsPlugin.getExtension(getProject()).getFileConstants();

        getInputs().files(fileConstants.versionFile);

        getOutputs().dir(fileConstants.libraries);

        getOutputs().upToDateWhen(task -> false);

        doLast(_this -> {
            if (!fileConstants.versionFile.exists()) {
                throw new RuntimeException("Can't download the jars without the " + fileConstants.versionFile.getName() + " file!");
            }
            try {
                Map<String, ?> version = (Map<String, ?>) new JsonSlurper().parseText(FileUtils.readFileToString(fileConstants.versionFile, StandardCharsets.UTF_8));

                getLogger().lifecycle(":downloading minecraft libraries");

                if (!fileConstants.libraries.exists()) {
                    fileConstants.libraries.mkdirs();
                }

                ((List<Map<String, Map<String, Map<String, String>>>>) version.get("libraries")).forEach(it -> {
                    String downloadUrl = it.get("downloads").get("artifact").get("url");

                    DownloadAction downloadAction = new DownloadAction(getProject(), _this);
                    downloadAction.src(downloadUrl);
                    downloadAction.dest(new File(fileConstants.libraries, downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1)));
                    downloadAction.overwrite(false);

                    try {
                        downloadAction.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    getProject().getDependencies().add("decompileClasspath", it.get("name"));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
