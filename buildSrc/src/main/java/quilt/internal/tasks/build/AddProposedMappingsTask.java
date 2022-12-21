package quilt.internal.tasks.build;

import java.io.File;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import cuchaz.enigma.command.InsertProposedMappingsCommand;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.format.Tiny2Reader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.VisibleForTesting;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public class AddProposedMappingsTask extends DefaultMappingsTask {
    @OutputFile
    public File outputMappings;

    @InputFile
    private final Property<File> inputJar;

    @InputFile
    private final Property<File> inputMappings;

    @InputFile
    private final Property<File> profile;

    public AddProposedMappingsTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        outputMappings = new File(fileConstants.tempDir, getName() + ".tiny");
        inputJar = getProject().getObjects().property(File.class);
        inputMappings = getProject().getObjects().property(File.class);
        profile = getProject().getObjects().property(File.class);
    }

    @TaskAction
    public void addProposedMappings() throws Exception {
        getLogger().lifecycle(":seeking auto-mappable entries");

        Path input = inputMappings.get().toPath();
        Path output = outputMappings.toPath();
        Path jar = inputJar.get().toPath();
        Path profilePath = profile.map(File::toPath).get();

        addProposedMappings(input, output, jar, profilePath);
    }

    @VisibleForTesting
    public static void addProposedMappings(Path input, Path output, Path jar, Path profilePath) throws Exception {
        String name = output.getFileName().toString();
        Path preprocessedMappings = output.getParent().resolve(name.replace(".tiny", "-preprocessed.tiny"));
        Path processedMappings = output.getParent().resolve(name.replace(".tiny", "-processed.tiny"));

        List<String> namespaces;
        try (Reader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            namespaces = Tiny2Reader.getNamespaces(reader);
        }

        if (!namespaces.contains("named")) {
            throw new IllegalArgumentException("Input mappings must contain the named namespace");
        }

        boolean extraProcessing = preprocessFile(input, preprocessedMappings);
        Path commandInput = extraProcessing ? preprocessedMappings : input;
        Path commandOutput = extraProcessing ? processedMappings : output;

        InsertProposedMappingsCommand.run(jar,
            commandInput,
            commandOutput,
            String.format("tinyv2:%s:named", namespaces.get(0)),
            profilePath,
            null);

        if (extraProcessing) {
            MemoryMappingTree outputTree = postProcessTree(input, processedMappings);
            try (MappingWriter writer = MappingWriter.create(output, MappingFormat.TINY_2)) {
                outputTree.accept(writer);
            }
        }
    }

    // Reorder dst namespaces to `<src-namespace> named [others...]`
    // Enigma doesn't support multiple dst namespaces, and just uses the first one
    private static boolean preprocessFile(Path inputMappings, Path output) throws Exception {
        MemoryMappingTree inputTree = new MemoryMappingTree();
        try (Reader reader = Files.newBufferedReader(inputMappings, StandardCharsets.UTF_8)) {
            Tiny2Reader.read(reader, inputTree);
        }

        // Reorder destination namespaces to put the named namespace first
        List<String> dstNamespaces = new ArrayList<>(inputTree.getDstNamespaces());
        if (!dstNamespaces.get(0).equals("named")) {
            MemoryMappingTree outputTree = new MemoryMappingTree();
            int i = dstNamespaces.indexOf("named");
            dstNamespaces.set(i, dstNamespaces.get(0));
            dstNamespaces.set(0, "named");
            inputTree.accept(new MappingDstNsReorder(outputTree, dstNamespaces));

            try (MappingWriter mappingWriter = MappingWriter.create(output, MappingFormat.TINY_2)) {
                outputTree.accept(mappingWriter);
            }

            return true;
        }

        return false;
    }

    // Merge input mappings with the proposed mappings to restore the lost namespaces
    private static MemoryMappingTree postProcessTree(Path inputMappings, Path processedMappings) throws Exception {
        MemoryMappingTree inputTree = new MemoryMappingTree();
        try (Reader reader = Files.newBufferedReader(inputMappings, StandardCharsets.UTF_8)) {
            Tiny2Reader.read(reader, inputTree);
        }

        MemoryMappingTree processedTree = new MemoryMappingTree();
        try (Reader reader = Files.newBufferedReader(processedMappings, StandardCharsets.UTF_8)) {
            Tiny2Reader.read(reader, processedTree);
        }

        // Merge trees
        MemoryMappingTree output = new MemoryMappingTree();
        inputTree.accept(output);
        // Merge processed tree after original to keep the original namespaces order
        processedTree.accept(output);

        return output;
    }

    public File getOutputMappings() {
        return outputMappings;
    }

    public void setOutputMappings(File outputMappings) {
        this.outputMappings = outputMappings;
    }

    public Property<File> getInputJar() {
        return inputJar;
    }

    public Property<File> getInputMappings() {
        return inputMappings;
    }

    public Property<File> getProfile() {
        return profile;
    }
}
