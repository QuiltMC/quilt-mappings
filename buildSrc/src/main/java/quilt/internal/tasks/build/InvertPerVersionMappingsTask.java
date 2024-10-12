package quilt.internal.tasks.build;

import java.io.File;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.VisibleForTesting;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import net.fabricmc.stitch.commands.tinyv2.CommandReorderTinyV2;

public abstract class InvertPerVersionMappingsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "invertPerVersionMappings";

    @InputFile
    public abstract RegularFileProperty getInput();

    @OutputFile
    public abstract RegularFileProperty getInvertedTinyFile();

    public InvertPerVersionMappingsTask() {
        super(Constants.Groups.BUILD_MAPPINGS);
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
