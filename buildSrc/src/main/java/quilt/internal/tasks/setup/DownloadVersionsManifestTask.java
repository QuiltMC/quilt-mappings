package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public class DownloadVersionsManifestTask extends DefaultMappingsTask {
    private final File manifestFile;

    public DownloadVersionsManifestTask() {
        super(Constants.Groups.SETUP_GROUP);
        this.getInputs().property("currenttime", new Date());

        manifestFile = new File(fileConstants.cacheFilesMinecraft, "version_manifest_v2.json");
        getOutputs().file(manifestFile);
    }

    @TaskAction
    public void downloadVersionsManifestTask() throws IOException {
        getLogger().lifecycle(":downloading minecraft versions manifest");
        startDownload()
                .src("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json")
                .dest(manifestFile)
                .overwrite(true)
                .download();
    }
}
