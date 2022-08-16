package quilt.internal.tasks.build;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.format.Tiny2Writer;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.setup.CheckIntermediaryMappingsTask;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;

public class RemoveIntermediaryTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "removeIntermediary";

    @InputFile
    private final RegularFileProperty input;

    @OutputFile
    public File outputMappings;

    public RemoveIntermediaryTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        dependsOn(CheckIntermediaryMappingsTask.TASK_NAME, MergeIntermediaryTask.TASK_NAME);
        onlyIf(task -> getTaskByType(CheckIntermediaryMappingsTask.class).isPresent());

        this.outputMappings = new File(fileConstants.tempDir, "intermediary.tiny");
        getOutputs().file(this.outputMappings);

        input = getProject().getObjects().fileProperty();
        input.convention(getTaskByType(MergeIntermediaryTask.class)::getOutputMappings);
    }

    @TaskAction
    public void removeIntermediary() throws Exception {
        File mappingsTinyInput = input.get().getAsFile();

        getLogger().lifecycle(":removing intermediary");
        MemoryMappingTree tree = new MemoryMappingTree(false);
        MappingReader.read(mappingsTinyInput.toPath(), MappingFormat.TINY_2, tree);
        try (Tiny2Writer w = new Tiny2Writer(Files.newBufferedWriter(outputMappings.toPath()), false)) {
            tree.accept(
                new MappingSourceNsSwitch(
                    new MappingDstNsReorder(w, Collections.singletonList("named")), // Remove official namespace
                    "intermediary"
                )
            );
        }
    }

    public RegularFileProperty getInput() {
        return input;
    }

    public File getOutputMappings() {
        return outputMappings;
    }
}
