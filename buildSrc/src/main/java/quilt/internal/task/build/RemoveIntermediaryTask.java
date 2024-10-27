package quilt.internal.task.build;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.format.tiny.Tiny2FileWriter;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import org.gradle.api.Task;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.jetbrains.annotations.VisibleForTesting;
import quilt.internal.Constants.Groups;
import quilt.internal.Constants.Namespaces;
import quilt.internal.plugin.MapIntermediaryPlugin;
import quilt.internal.task.DefaultMappingsTask;
import quilt.internal.task.setup.IntermediaryDependantTask;
import quilt.internal.util.ProviderUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

/**
 * TODO is this (and the name) accurate? It looks like it actually replaces official with intermediary.<br><br>
 * Removes the {@value Namespaces#INTERMEDIARY} namespace from the {@link #getInput() input} mappings.
 *
 * @see MapIntermediaryPlugin MapIntermediaryPlugin's configureEach
 */
public abstract class RemoveIntermediaryTask extends DefaultMappingsTask implements IntermediaryDependantTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapIntermediaryPlugin}.
     */
    public static final String REMOVE_INTERMEDIARY_TASK_NAME = "removeIntermediary";

    @InputFile
    public abstract RegularFileProperty getInput();

    @OutputFile
    public abstract RegularFileProperty getOutputMappings();

    public RemoveIntermediaryTask() {
        super(Groups.BUILD_MAPPINGS);
    }

    @TaskAction
    public void removeIntermediary() throws Exception {
        final Path mappingsTinyInput = ProviderUtil.getPath(this.getInput());
        final Path output = ProviderUtil.getPath(this.getOutputMappings());

        this.getLogger().lifecycle(":removing " + Namespaces.INTERMEDIARY);
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
                    new MappingDstNsReorder(w, Collections.singletonList(Namespaces.NAMED)),
                    Namespaces.INTERMEDIARY
                )
            );
        }
    }
}
