package quilt.internal.tasks.build;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.format.tiny.Tiny2FileWriter;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.VisibleForTesting;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.setup.CheckIntermediaryMappingsTask;
import quilt.internal.util.PropertyUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public abstract class RemoveIntermediaryTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "removeIntermediary";

    @InputFile
    public abstract RegularFileProperty getInput();

    @OutputFile
    public abstract RegularFileProperty getOutputMappings();

    public RemoveIntermediaryTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        this.dependsOn(CheckIntermediaryMappingsTask.TASK_NAME, MergeIntermediaryTask.TASK_NAME);
        this.onlyIf(task ->
            this.getTaskNamed(CheckIntermediaryMappingsTask.TASK_NAME, CheckIntermediaryMappingsTask.class).isPresent()
        );

        this.getOutputMappings().convention(() ->
            new File(this.fileConstants.buildDir, "mappings-intermediary.tiny")
        );

        this.getInput().convention(
            this.getTaskNamed(MergeIntermediaryTask.TASK_NAME, MergeIntermediaryTask.class).getOutputMappings()
        );
    }

    @TaskAction
    public void removeIntermediary() throws Exception {
        final Path mappingsTinyInput = PropertyUtil.getPath(this.getInput());
        final Path output = PropertyUtil.getPath(this.getOutputMappings());

        this.getLogger().lifecycle(":removing intermediary");
        removeIntermediary(mappingsTinyInput, output);
    }

    @VisibleForTesting
    public static void removeIntermediary(Path mappingsTinyInput, Path output) throws IOException {
        final MemoryMappingTree tree = new MemoryMappingTree(false);
        MappingReader.read(mappingsTinyInput, MappingFormat.TINY_2_FILE, tree);
        try (Tiny2FileWriter w = new Tiny2FileWriter(Files.newBufferedWriter(output), false)) {
            tree.accept(
                new MappingSourceNsSwitch(
                    // Remove official namespace
                    new MappingDstNsReorder(w, Collections.singletonList("named")),
                    "intermediary"
                )
            );
        }
    }
}
