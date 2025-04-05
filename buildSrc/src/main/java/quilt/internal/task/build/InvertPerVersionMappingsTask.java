package quilt.internal.task.build;

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskContainer;
import org.jetbrains.annotations.VisibleForTesting;
import quilt.internal.constants.Groups;
import quilt.internal.constants.Namespaces;
import quilt.internal.plugin.MapMinecraftJarsPlugin;

import net.fabricmc.stitch.commands.tinyv2.CommandReorderTinyV2;

public abstract class InvertPerVersionMappingsTask extends DefaultTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapMinecraftJarsPlugin}.
     */
    public static final String INVERT_INTERMEDIATE_MAPPINGS_TASK_NAME = "invertIntermediateMappings";

    @InputFile
    public abstract RegularFileProperty getInput();

    @OutputFile
    public abstract RegularFileProperty getInvertedTinyFile();

    public InvertPerVersionMappingsTask() {
        this.setGroup(Groups.BUILD_MAPPINGS);
    }

    @TaskAction
    public void invertPerVersionMappings() throws Exception {
        this.getLogger().lifecycle(":building inverted {}", Namespaces.INTERMEDIATE);

        invertMappings(this.getInput().get().getAsFile(), this.getInvertedTinyFile().get().getAsFile());
    }

    @VisibleForTesting
    public static void invertMappings(File input, File output) throws Exception {
        final String[] args = {
                input.getAbsolutePath(), output.getAbsolutePath(),
                Namespaces.INTERMEDIATE, Namespaces.OFFICIAL
        };

        new CommandReorderTinyV2().run(args);
    }
}
