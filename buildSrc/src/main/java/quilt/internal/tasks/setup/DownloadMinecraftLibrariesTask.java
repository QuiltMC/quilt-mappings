package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.util.JsonUtils;

public class DownloadMinecraftLibrariesTask extends DefaultMappingsTask {
    public DownloadMinecraftLibrariesTask() {
        super(Constants.Groups.SETUP_GROUP);
        dependsOn("downloadWantedVersionManifest");

        getInputs().files(fileConstants.versionFile);

        getOutputs().dir(fileConstants.libraries);
        outputsNeverUpToDate();
    }

    @TaskAction
    public void downloadMinecraftLibrariesTask() throws IOException {
        if (!fileConstants.versionFile.exists()) {
            throw new RuntimeException("Can't download the jars without the " + fileConstants.versionFile.getName() + " file!");
        }
        Map<String, ?> version = JsonUtils.getFromTree(FileUtils.readFileToString(fileConstants.versionFile, StandardCharsets.UTF_8));

        getLogger().lifecycle(":downloading minecraft libraries");

        if (!fileConstants.libraries.exists()) {
            fileConstants.libraries.mkdirs();
        }

        for (var library : JsonUtils.<List<Map<String, Map<String, Map<String, String>>>>>getFromTree(version, "libraries")) {
            String downloadUrl = JsonUtils.getFromTree(library, "downloads", "artifact", "url");

            startDownload()
                    .src(downloadUrl)
                    .dest(new File(fileConstants.libraries, downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1)))
                    .overwrite(false)
                    .download();

            getProject().getDependencies().add("decompileClasspath", library.get("name"));
        }
    }
}
