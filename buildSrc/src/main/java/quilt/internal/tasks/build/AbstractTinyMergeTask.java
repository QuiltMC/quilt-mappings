package quilt.internal.tasks.build;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.adapter.MappingNsCompleter;
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

public abstract class AbstractTinyMergeTask extends DefaultMappingsTask {
    @InputFile
    protected final RegularFileProperty input;

    @OutputFile
    public File outputMappings;

    private final String mergeName;

    public AbstractTinyMergeTask(String outputMappings, String mergeName) {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        this.outputMappings = new File(fileConstants.tempDir, outputMappings);
        getOutputs().file(this.outputMappings);

        input = getProject().getObjects().fileProperty();

        this.mergeName = mergeName;
    }

    @TaskAction
    public abstract void mergeMappings() throws Exception;

    protected void mergeMappings(File mergeTinyInput) throws Exception {
        File mappingsTinyInput = input.get().getAsFile();

        getLogger().lifecycle(":merging {} and {}", Constants.MAPPINGS_NAME, this.mergeName);
        MemoryMappingTree tree = new MemoryMappingTree(false); // hashed is the src namespace
        MappingReader.read(mergeTinyInput.toPath(), MappingFormat.TINY_2, tree);
        MappingReader.read(mappingsTinyInput.toPath(), MappingFormat.TINY_2, tree);
        try (Tiny2Writer w = new Tiny2Writer(Files.newBufferedWriter(outputMappings.toPath()), false)) {
            tree.accept(
                new MappingNsCompleter(
                    new MappingSourceNsSwitch(w, "official", /*Drop methods not in hashed*/ true),
                    Collections.singletonMap("named", this.mergeName), // Fill unnamed classes with hashed; Needed for remapUnpickDefinitions and possibly other things
                    false
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
