package quilt.internal.tasks.build;

import java.io.File;

import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import net.fabricmc.stitch.commands.tinyv2.CommandMergeTinyV2;
import net.fabricmc.stitch.commands.tinyv2.CommandReorderTinyV2;

public class MergeTinyTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "mergeTiny";

    @OutputFile
    public File mergedTiny = new File(fileConstants.tempDir, "mappings.tiny");

    public MergeTinyTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        outputsNeverUpToDate();
        getOutputs().file(mergedTiny);
        dependsOn(InvertPerVersionMappingsTask.TASK_NAME, BuildMappingsTinyTask.TASK_NAME);
    }

    @TaskAction
    public void mergeMappings() throws Exception {
        File mappingsTinyInput = this.getTaskFromType(BuildMappingsTinyTask.class).outputMappings;
        File hashedTinyInput = this.getTaskFromType(InvertPerVersionMappingsTask.class).invertedTinyFile;

        File unorderedResultMappings = new File(fileConstants.tempDir, "mappings-unordered.tiny");

        getLogger().lifecycle(":merging {} and {}", Constants.MAPPINGS_NAME, Constants.PER_VERSION_MAPPINGS_NAME);
        String[] args = {
                hashedTinyInput.getAbsolutePath(),
                mappingsTinyInput.getAbsolutePath(),
                unorderedResultMappings.getAbsolutePath(),
                Constants.PER_VERSION_MAPPINGS_NAME,
                "official"
        };

        new CommandMergeTinyV2().run(args);

        getLogger().lifecycle(":reordering merged {}", Constants.PER_VERSION_MAPPINGS_NAME);
        String[] args2 = {
                unorderedResultMappings.getAbsolutePath(),
                mergedTiny.getAbsolutePath(),
                "official", Constants.PER_VERSION_MAPPINGS_NAME, "named"
        };

        new CommandReorderTinyV2().run(args2);
    }

    public File getMergedTiny() {
        return mergedTiny;
    }
}
