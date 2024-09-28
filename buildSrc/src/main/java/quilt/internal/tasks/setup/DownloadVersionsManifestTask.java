package quilt.internal.tasks.setup;

import java.io.IOException;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public abstract class DownloadVersionsManifestTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadVersionsManifest";

    @OutputFile
    public abstract RegularFileProperty getManifestFile();

    public DownloadVersionsManifestTask() {
        super(Constants.Groups.SETUP_GROUP);
    }

    @TaskAction
    public void downloadVersionsManifestTask() throws IOException {
        this.getLogger().lifecycle(":downloading minecraft versions manifest");
        this.startDownload()
                .src("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")
                .dest(this.getManifestFile().get().getAsFile())
                .overwrite(true)
                .download();
    }
}
