package quilt.internal.tasks.build;

import java.io.File;

import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import net.fabricmc.stitch.commands.tinyv2.CommandReorderTinyV2;

public class InvertPerVersionMappingsTask extends DefaultMappingsTask {
    @OutputFile
    public File invertedTinyFile = new File(fileConstants.cacheFilesMinecraft, Constants.MINECRAFT_VERSION + "-" + Constants.PER_VERSION_MAPPINGS_NAME +"-inverted.tiny");

    public InvertPerVersionMappingsTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);

        outputsNeverUpToDate();
    }

    @TaskAction
    public void invertPerVersionMappings() throws Exception {
        getLogger().lifecycle(":building inverted hashed mojmap");

        File input = this.<DownloadPerVersionMappingsTask>getTaskFromName("downloadHashedMojmap").getTinyFile();

        String[] args = {
                input.getAbsolutePath(),
                invertedTinyFile.getAbsolutePath(),
                "hashed", "official"
        };

        new CommandReorderTinyV2().run(args);
    }

    public File getInvertedTinyFile() {
        return invertedTinyFile;
    }
}
