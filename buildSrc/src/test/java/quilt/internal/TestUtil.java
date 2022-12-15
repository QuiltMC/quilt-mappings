package quilt.internal;

import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.format.EnigmaReader;
import net.fabricmc.mappingio.format.EnigmaWriter;
import net.fabricmc.mappingio.format.Tiny2Reader;
import net.fabricmc.mappingio.format.Tiny2Writer;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestUtil {
    private static MemoryMappingTree readTree(Path path, MappingFormatReader formatReader) throws IOException {
        MemoryMappingTree tree = new MemoryMappingTree();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            formatReader.read(reader, tree);
        }

        return tree;
    }

    private static void writeTree(Path path, MappingTree tree, MappingFormatWriter formatWriter) throws IOException {
        Path abs = path.toAbsolutePath();
        if (abs.getParent() != null && !Files.exists(abs.getParent())) {
            Files.createDirectories(abs.getParent());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            tree.accept(formatWriter.getWriter(writer));
        }
    }

    public static MemoryMappingTree readTinyV2(Path path) throws IOException {
        return readTree(path, Tiny2Reader::read);
    }

    public static void writeTinyV2(Path path, MappingTree tree) throws IOException {
        writeTree(path, tree, writer -> new Tiny2Writer(writer, false));
    }

    public static MemoryMappingTree readEnigma(Path path) throws IOException {
        MemoryMappingTree tree = new MemoryMappingTree();
        EnigmaReader.read(path, tree);
        return tree;
    }

    public static void writeEnigma(Path path, MappingTree tree) throws IOException {
        tree.accept(new EnigmaWriter(path, true));
    }

    @FunctionalInterface
    public interface MappingFormatReader {
        void read(Reader reader, MappingVisitor visitor) throws IOException;
    }

    @FunctionalInterface
    public interface MappingFormatWriter {
        MappingVisitor getWriter(Writer writer) throws IOException;
    }
}
