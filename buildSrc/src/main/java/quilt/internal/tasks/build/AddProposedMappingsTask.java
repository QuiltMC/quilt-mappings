package quilt.internal.tasks.build;

import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.quiltmc.enigma.api.Enigma;
import org.quiltmc.enigma.api.EnigmaProfile;
import org.quiltmc.enigma.api.EnigmaProject;
import org.quiltmc.enigma.command.Command;
import org.quiltmc.enigma.command.FillClassMappingsCommand;
import org.quiltmc.enigma.command.CommandsUtil;
import org.quiltmc.enigma.api.translation.mapping.EntryMapping;
import org.quiltmc.enigma.api.translation.mapping.MappingDelta;
import org.quiltmc.enigma.api.translation.mapping.serde.MappingSaveParameters;
import org.quiltmc.enigma.api.translation.mapping.tree.DeltaTrackingTree;
import org.quiltmc.enigma.api.translation.mapping.tree.EntryTree;
import org.quiltmc.enigma.util.EntryTreePrinter;
import org.quiltmc.enigma.util.Utils;
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
        outputMappings = new File(fileConstants.buildDir, getName() + ".tiny");
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

        addProposedMappings(input, output, fileConstants.tempDir.toPath(), jar, profilePath);
    }

    @VisibleForTesting
    public static void addProposedMappings(Path input, Path output, Path tempDir, Path jar, Path profilePath) throws Exception {
        String name = output.getFileName().toString();
        Path preprocessedMappings = tempDir.resolve(name.replace(".tiny", "-preprocessed.tiny"));
        Path processedMappings = tempDir.resolve(name.replace(".tiny", "-processed.tiny"));

        List<String> namespaces;
        try (Reader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            namespaces = Tiny2Reader.getNamespaces(reader);
        }

        if (!namespaces.contains("named")) {
            throw new IllegalArgumentException("Input mappings must contain the named namespace");
        }

        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }

        boolean extraProcessing = preprocessFile(input, preprocessedMappings);
        Path commandInput = extraProcessing ? preprocessedMappings : input;
        Path commandOutput = extraProcessing ? processedMappings : output;

        runCommands(jar,
            commandInput,
            commandOutput,
            profilePath,
            namespaces.get(0),
            "named"
        );

        if (extraProcessing) {
            MemoryMappingTree outputTree = postProcessTree(input, processedMappings);
            try (MappingWriter writer = MappingWriter.create(output, MappingFormat.TINY_2)) {
                outputTree.accept(writer);
            }
        }
    }

    private static void runCommands(Path jar, Path input, Path output, Path profilePath, String fromNamespace, String toNamespace) throws Exception {
        EnigmaProfile profile = EnigmaProfile.read(profilePath);
        Enigma enigma = Command.createEnigma(profile, null);

        EnigmaProject project = Command.openProject(jar, input, enigma);

        boolean debug = System.getProperty("qm.addProposedMappings.debug", "false").toLowerCase(Locale.ROOT).equals("true");
        EntryTree<EntryMapping> withProposals = project.getRemapper().getMappings(); // Proposed names are automatically added when opening a project

        // TODO: Disable fillAll after fixing the tiny v2 writer to avoid adding unnecessary class names
        EntryTree<EntryMapping> result = FillClassMappingsCommand.exec(project.getJarIndex(), withProposals, true, debug);

        Utils.delete(output);
        MappingSaveParameters profileParameters = enigma.getProfile().getMappingSaveParameters();
        MappingSaveParameters saveParameters = new MappingSaveParameters(profileParameters.fileNameFormat(), /*writeProposedNames*/ true, fromNamespace, toNamespace);
        CommandsUtil.getReadWriteService(enigma, output).write(result, output, saveParameters);

        if (debug) {
            Path deltaFile = output.getParent().resolve(output.getFileName().toString() + "-fill-delta.txt");
            MappingDelta<EntryMapping> fillDelta = ((DeltaTrackingTree<EntryMapping>) result).takeDelta();

            try (BufferedWriter writer = Files.newBufferedWriter(deltaFile)) {
                List<String> content = fillDelta.getChanges().getAllEntries()
                        .map(Objects::toString)
                        .toList();

                for (String s : content) {
                    writer.write(s);
                    writer.newLine();
                }
            }

            Path debugFile = output.getParent().resolve(output.getFileName().toString() + "-tree.txt");
            try (BufferedWriter writer = Files.newBufferedWriter(debugFile)) {
                EntryTreePrinter.print(new PrintWriter(writer), project.getRemapper().getProposedMappings());
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
