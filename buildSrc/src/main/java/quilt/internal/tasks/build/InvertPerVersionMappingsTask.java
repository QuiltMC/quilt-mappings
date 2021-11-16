package quilt.internal.tasks.build;

import java.io.File;

import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.setup.DownloadPerVersionMappingsTask;

import net.fabricmc.stitch.commands.tinyv2.CommandReorderTinyV2;

public class InvertPerVersionMappingsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "invertHashedMojmap";

    @OutputFile
    public File invertedTinyFile = new File(fileConstants.cacheFilesMinecraft, String.format("%s-%s-inverted.tiny", Constants.MINECRAFT_VERSION, Constants.PER_VERSION_MAPPINGS_NAME));

    public InvertPerVersionMappingsTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);

        outputsNeverUpToDate();
    }

    @TaskAction
    public void invertPerVersionMappings() throws Exception {
        getLogger().lifecycle(":building inverted {}", Constants.PER_VERSION_MAPPINGS_NAME);

        File input = this.getTaskByType(DownloadPerVersionMappingsTask.class).getTinyFile();

        String[] args = {
                input.getAbsolutePath(),
                invertedTinyFile.getAbsolutePath(),
                Constants.PER_VERSION_MAPPINGS_NAME, "official"
        };

        new CommandReorderTinyV2().run(args);
    }

    public File getInvertedTinyFile() {
        return invertedTinyFile;
    }
}
