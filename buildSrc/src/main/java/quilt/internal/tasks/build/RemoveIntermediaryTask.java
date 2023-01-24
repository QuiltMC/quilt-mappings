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
import org.jetbrains.annotations.VisibleForTesting;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.setup.CheckIntermediaryMappingsTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

        this.outputMappings = new File(fileConstants.buildDir, "mappings-intermediary.tiny");
        getOutputs().file(this.outputMappings);

        input = getProject().getObjects().fileProperty();
        input.convention(getTaskByType(MergeIntermediaryTask.class)::getOutputMappings);
    }

    @TaskAction
    public void removeIntermediary() throws Exception {
        Path mappingsTinyInput = input.get().getAsFile().toPath();
        Path output = outputMappings.toPath();

        getLogger().lifecycle(":removing intermediary");
        removeIntermediary(mappingsTinyInput, output);
    }

    @VisibleForTesting
    public static void removeIntermediary(Path mappingsTinyInput, Path output) throws IOException {
        MemoryMappingTree tree = new MemoryMappingTree(false);
        MappingReader.read(mappingsTinyInput, MappingFormat.TINY_2, tree);
        try (Tiny2Writer w = new Tiny2Writer(Files.newBufferedWriter(output), false)) {
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
