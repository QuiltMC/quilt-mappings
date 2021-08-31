package quilt.internal.tasks.build;

import java.io.File;

import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import net.fabricmc.stitch.commands.tinyv2.CommandMergeTinyV2;
import net.fabricmc.stitch.commands.tinyv2.CommandReorderTinyV2;

public class MergeTinyTask extends DefaultMappingsTask {
    @OutputFile
    public File mergedTiny = new File(fileConstants.tempDir, "mappings.tiny");

    public MergeTinyTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        outputsNeverUpToDate();
        getOutputs().file(mergedTiny);
        dependsOn("invertHashedMojmap", "buildMappingsTiny");
    }

    @TaskAction
    public void mergeMappings() throws Exception {
        File mappingsTinyInput = this.<BuildMappingsTinyTask>getTaskFromName("buildMappingsTiny").outputMappings;
        File hashedTinyInput = this.<InvertPerVersionMappingsTask>getTaskFromName("invertHashedMojmap").invertedTinyFile;

        File unorderedResultMappings = new File(fileConstants.tempDir, "mappings-unordered.tiny");

        getLogger().lifecycle(":merging quilt-mappings and hashed");
        String[] args = {
                hashedTinyInput.getAbsolutePath(),
                mappingsTinyInput.getAbsolutePath(),
                unorderedResultMappings.getAbsolutePath(),
                "hashed",
                "official"
        };

        new CommandMergeTinyV2().run(args);

        getLogger().lifecycle(":reordering merged hashed");
        String[] args2 = {
                unorderedResultMappings.getAbsolutePath(),
                mergedTiny.getAbsolutePath(),
                "official", "hashed", "named"
        };

        new CommandReorderTinyV2().run(args2);
    }

    public File getMergedTiny() {
        return mergedTiny;
    }
}
