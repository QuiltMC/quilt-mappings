package quilt.internal.tasks.build;

import java.io.File;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.VisibleForTesting;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.setup.DownloadPerVersionMappingsTask;

import net.fabricmc.stitch.commands.tinyv2.CommandReorderTinyV2;

public class InvertPerVersionMappingsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "invertPerVersionMappings";

    @InputFile
    private final RegularFileProperty input;

    @OutputFile
    public File invertedTinyFile = new File(fileConstants.cacheFilesMinecraft, String.format("%s-%s-inverted.tiny", Constants.MINECRAFT_VERSION, Constants.PER_VERSION_MAPPINGS_NAME));

    public InvertPerVersionMappingsTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        this.dependsOn(DownloadPerVersionMappingsTask.TASK_NAME);

        input = getProject().getObjects().fileProperty();
        input.convention(getTaskByType(DownloadPerVersionMappingsTask.class)::getTinyFile);
    }

    @TaskAction
    public void invertPerVersionMappings() throws Exception {
        getLogger().lifecycle(":building inverted {}", Constants.PER_VERSION_MAPPINGS_NAME);

        invertMappings(input.get().getAsFile(), invertedTinyFile);
    }

    @VisibleForTesting
    public static void invertMappings(File input, File output) throws Exception {
        String[] args = {
                input.getAbsolutePath(),
                output.getAbsolutePath(),
                Constants.PER_VERSION_MAPPINGS_NAME, "official"
        };

        new CommandReorderTinyV2().run(args);
    }

    public File getInvertedTinyFile() {
        return invertedTinyFile;
    }

    public RegularFileProperty getInput() {
        return input;
    }
}
