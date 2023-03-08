package quilt.internal.tasks.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import quilt.internal.Constants;
import quilt.internal.mappingio.CompleteInitializersVisitor;
import quilt.internal.tasks.DefaultMappingsTask;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingNsCompleter;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.format.Tiny2Writer;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public abstract class AbstractTinyMergeTask extends DefaultMappingsTask {
    @InputFile
    protected final RegularFileProperty input;

    @OutputFile
    public File outputMappings;

    protected final String mergeName;
    protected final String fillName;

    public AbstractTinyMergeTask(String outputMappings, String mergeName) {
        this(outputMappings, mergeName, mergeName);
    }

    public AbstractTinyMergeTask(String outputMappings, String mergeName, String fillName) {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        this.outputMappings = new File(fileConstants.buildDir, outputMappings);
        getOutputs().file(this.outputMappings);

        input = getProject().getObjects().fileProperty();

        this.mergeName = mergeName;
        this.fillName = fillName;
    }

    @TaskAction
    public abstract void mergeMappings() throws Exception;

    protected void mergeMappings(File mergeTinyInput) throws Exception {
        File mappingsTinyInput = input.get().getAsFile();

        getLogger().lifecycle(":merging {} and {}", Constants.MAPPINGS_NAME, this.mergeName);
        mergeMappings(mappingsTinyInput.toPath(), mergeTinyInput.toPath(), outputMappings.toPath(),
            this::getFirstVisitor, this::getPreWriteVisitor);
    }

    @VisibleForTesting
    public static void mergeMappings(Path mappingsTinyInput, Path mergeTinyInput, Path outputMappings,
                                     Function<MappingVisitor, MappingVisitor> firstVisitor,
                                     Function<MappingVisitor, MappingVisitor> preWriteVisitor) throws IOException {
        MemoryMappingTree tree = new MemoryMappingTree(false); // hashed is the src namespace
        MappingReader.read(mergeTinyInput, MappingFormat.TINY_2, tree);
        MappingReader.read(mappingsTinyInput, MappingFormat.TINY_2, tree);
        try (Tiny2Writer w = new Tiny2Writer(Files.newBufferedWriter(outputMappings), false)) {
            tree.accept(firstVisitor.apply(
                new CompleteInitializersVisitor(
                    new MappingSourceNsSwitch(preWriteVisitor.apply(w), "official", /*Drop methods not in hashed*/ true)
                )
            ));
        }
    }

    protected MappingVisitor getFirstVisitor(MappingVisitor next) {
        return next;
    }

    protected MappingVisitor getPreWriteVisitor(MappingVisitor writer) {
        return writer;
    }

    public RegularFileProperty getInput() {
        return input;
    }

    @NotNull
    public File getOutputMappings() {
        return outputMappings;
    }
}
