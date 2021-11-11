package quilt.internal.tasks.build;

import java.io.File;

import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import net.fabricmc.stitch.commands.tinyv2.CommandMergeTinyV2;
import net.fabricmc.stitch.commands.tinyv2.CommandReorderTinyV2;

public abstract class AbstractTinyMergeTask extends DefaultMappingsTask {
    @OutputFile
    public File outputMappings;

    public AbstractTinyMergeTask(String outputMappings) {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        outputsNeverUpToDate();
        this.outputMappings = new File(fileConstants.tempDir, outputMappings);
        getOutputs().file(this.outputMappings);
    }

    @TaskAction
    public void mergeMappings() throws Exception {
        File hashedTinyInput = this.getTaskByType(InvertPerVersionMappingsTask.class).getInvertedTinyFile();
        File mappingsTinyInput = getInputTinyFile();

        File temporaryResultMappings = new File(fileConstants.tempDir, "mappings-" + getName() + ".tiny");

        getLogger().lifecycle(":merging {} and {}", Constants.MAPPINGS_NAME, Constants.PER_VERSION_MAPPINGS_NAME);
        String[] args = {
                hashedTinyInput.getAbsolutePath(),
                mappingsTinyInput.getAbsolutePath(),
                temporaryResultMappings.getAbsolutePath(),
                Constants.PER_VERSION_MAPPINGS_NAME,
                "official"
        };

        new CommandMergeTinyV2().run(args);

        getLogger().lifecycle(":reordering merged {}", Constants.PER_VERSION_MAPPINGS_NAME);
        String[] args2 = {
                temporaryResultMappings.getAbsolutePath(),
                outputMappings.getAbsolutePath(),
                "official", Constants.PER_VERSION_MAPPINGS_NAME, "named"
        };

        new CommandReorderTinyV2().run(args2);
    }

    @Internal
    public abstract File getInputTinyFile();

    public File getOutputMappings() {
        return outputMappings;
    }
}

