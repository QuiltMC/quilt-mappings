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

public abstract class InvertPerVersionMappingsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "invertPerVersionMappings";

    @InputFile
    protected abstract RegularFileProperty getInput();

    @OutputFile
    public abstract RegularFileProperty getInvertedTinyFile();

    public InvertPerVersionMappingsTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        this.dependsOn(DownloadPerVersionMappingsTask.TASK_NAME);

        this.getInvertedTinyFile().convention(() -> new File(
            this.fileConstants.cacheFilesMinecraft,
            "%s-%s-inverted.tiny".formatted(Constants.MINECRAFT_VERSION, Constants.PER_VERSION_MAPPINGS_NAME)
        ));

        this.getInput().convention(
            this.getTaskNamed(DownloadPerVersionMappingsTask.TASK_NAME, DownloadPerVersionMappingsTask.class)
                .getTinyFile()
        );
    }

    @TaskAction
    public void invertPerVersionMappings() throws Exception {
        this.getLogger().lifecycle(":building inverted {}", Constants.PER_VERSION_MAPPINGS_NAME);

        invertMappings(this.getInput().get().getAsFile(), this.getInvertedTinyFile().get().getAsFile());
    }

    @VisibleForTesting
    public static void invertMappings(File input, File output) throws Exception {
        final String[] args = {
                input.getAbsolutePath(),
                output.getAbsolutePath(),
                Constants.PER_VERSION_MAPPINGS_NAME, "official"
        };

        new CommandReorderTinyV2().run(args);
    }
}
