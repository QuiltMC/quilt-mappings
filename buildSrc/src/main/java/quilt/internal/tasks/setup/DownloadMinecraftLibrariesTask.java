package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.util.JsonUtils;

public class DownloadMinecraftLibrariesTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadMinecraftLibraries";

    public DownloadMinecraftLibrariesTask() {
        super(Constants.Groups.SETUP_GROUP);
        dependsOn(DownloadWantedVersionManifestTask.TASK_NAME);

        getInputs().files(fileConstants.versionFile);

        getOutputs().dir(fileConstants.libraries);
        outputsNeverUpToDate();
    }

    @TaskAction
    public void downloadMinecraftLibrariesTask() throws IOException {
        if (!fileConstants.versionFile.exists()) {
            throw new RuntimeException(String.format("Can't download the jars without the %s file!", fileConstants.versionFile.getName()));
        }
        Map<String, ?> version = JsonUtils.getFromTree(FileUtils.readFileToString(fileConstants.versionFile, StandardCharsets.UTF_8));

        getLogger().lifecycle(":downloading minecraft libraries");

        if (!fileConstants.libraries.exists()) {
            fileConstants.libraries.mkdirs();
        }

        AtomicBoolean failed = new AtomicBoolean(false);

        Object lock = new Object();

        JsonUtils.<List<Map<String, Map<String, Map<String, String>>>>>getFromTree(version, "libraries").parallelStream().forEach(library -> {
            String downloadUrl = JsonUtils.getFromTree(library, "downloads", "artifact", "url");

            try {
                startDownload()
                        .src(downloadUrl)
                        .dest(new File(fileConstants.libraries, downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1)))
                        .overwrite(false)
                        .download();
            } catch (IOException e) {
                failed.set(true);
                e.printStackTrace();
            }
            synchronized (lock) {
                getProject().getDependencies().add("decompileClasspath", library.get("name"));
            }
        });

        if (failed.get()) {
            throw new RuntimeException("Unable to download libraries for specified minecraft version");
        }
    }
}
