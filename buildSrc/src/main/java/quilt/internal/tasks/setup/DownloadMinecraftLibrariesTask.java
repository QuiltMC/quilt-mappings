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
import quilt.internal.util.json.JsonUtils;
import quilt.internal.util.json.VersionFile;

public class DownloadMinecraftLibrariesTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadMinecraftLibraries";

    public DownloadMinecraftLibrariesTask() {
        super(Constants.Groups.SETUP_GROUP);
        dependsOn(DownloadWantedVersionManifestTask.TASK_NAME);

        getOutputs().dir(fileConstants.libraries);
        outputsNeverUpToDate();
    }

    @TaskAction
    public void downloadMinecraftLibrariesTask() throws IOException {
        VersionFile file = VersionFile.fromJson(FileUtils.readFileToString(getTaskFromType(DownloadWantedVersionManifestTask.class).getVersionFile(), StandardCharsets.UTF_8));

        getLogger().lifecycle(":downloading minecraft libraries");

        if (!fileConstants.libraries.exists()) {
            fileConstants.libraries.mkdirs();
        }

        AtomicBoolean failed = new AtomicBoolean(false);

        Object lock = new Object();

        file.libraries().parallelStream().forEach(library -> {
            try {
                startDownload()
                        .src(library.url())
                        .dest(new File(fileConstants.libraries, library.url().substring(library.url().lastIndexOf("/") + 1)))
                        .overwrite(false)
                        .download();
            } catch (IOException e) {
                failed.set(true);
                e.printStackTrace();
            }
            synchronized (lock) {
                getProject().getDependencies().add("decompileClasspath", library.name());
            }
        });

        if (failed.get()) {
            throw new RuntimeException("Unable to download libraries for specified minecraft version");
        }
    }
}
