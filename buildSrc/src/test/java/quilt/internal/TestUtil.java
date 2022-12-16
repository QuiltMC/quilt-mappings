package quilt.internal;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.format.EnigmaReader;
import net.fabricmc.mappingio.format.EnigmaWriter;
import net.fabricmc.mappingio.format.MappingFormat;
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
    private static MemoryMappingTree readTree(Path path, MappingFormat format) throws IOException {
        MemoryMappingTree tree = new MemoryMappingTree();
        MappingReader.read(path, format, tree);
        return tree;
    }

    private static void writeTree(Path path, MappingTree tree, MappingFormat format) throws IOException {
        Path abs = path.toAbsolutePath();
        if (abs.getParent() != null && !Files.exists(abs.getParent())) {
            Files.createDirectories(abs.getParent());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            tree.accept(MappingWriter.create(writer, format));
        }
    }

    public static MemoryMappingTree readTinyV2(Path path) throws IOException {
        return readTree(path, MappingFormat.TINY_2);
    }

    public static void writeTinyV2(Path path, MappingTree tree) throws IOException {
        writeTree(path, tree, MappingFormat.TINY_2);
    }

    public static MemoryMappingTree readEnigma(Path path) throws IOException {
        return readTree(path, MappingFormat.ENIGMA);
    }

    public static void writeEnigma(Path path, MappingTree tree) throws IOException {
        writeTree(path, tree, MappingFormat.ENIGMA);
    }
}
