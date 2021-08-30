package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import quilt.internal.Constants;
import quilt.internal.MappingsPlugin;

public class DownloadVersionsManifestTask extends DefaultTask {

    public DownloadVersionsManifestTask() {
        this.setGroup(Constants.Groups.SETUP_GROUP);
        this.getInputs().property("currenttime", new Date());

        File manifestFile = new File(MappingsPlugin.getExtension(getProject()).getFileConstants().cacheFilesMinecraft, "version_manifest_v2.json");
        getOutputs().file(manifestFile);

        doLast(_this -> {
            _this.getLogger().lifecycle(":downloading minecraft versions manifest");
            try {
                FileUtils.copyURLToFile(new URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"), manifestFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
