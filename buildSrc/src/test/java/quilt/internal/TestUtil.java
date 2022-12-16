package quilt.internal;

import daomephsta.unpick.api.IClassResolver;
import daomephsta.unpick.impl.constantmappers.datadriven.parser.v2.V2Parser;
import daomephsta.unpick.impl.representations.AbstractConstantGroup;
import daomephsta.unpick.impl.representations.TargetMethods;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import quilt.internal.util.UnpickFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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

    public static UnpickFile readUnpickFile(Path path, Path jar) throws Exception {
        HashMap<String, AbstractConstantGroup<?>> constantGroups = new HashMap<>();
        TargetMethods.Builder methodsBuilder = TargetMethods.builder(getJarClassResolver(jar));

        try (InputStream stream = Files.newInputStream(path)) {
            V2Parser.parse(stream, constantGroups, methodsBuilder);
        }

        return new UnpickFile(constantGroups, methodsBuilder);
    }

    private static IClassResolver getJarClassResolver(Path jar) throws Exception {
        ClassLoader loader = new URLClassLoader(new URL[]{new URL(null, jar.toUri().toString())});
        return new IClassResolver() {
            private final Map<String, ClassNode> cache = new HashMap<>();

            @Override
            public ClassReader resolveClassReader(String binaryName) throws ClassResolutionException {
                InputStream stream = loader.getResourceAsStream(binaryName.replace('.', '/') + ".class");

                if (stream != null) {
                    try {
                        return new ClassReader(stream);
                    } catch (IOException e) {
                        throw new ClassResolutionException(e);
                    }
                }

                throw new ClassResolutionException("Could not resolve class " + binaryName);
            }

            @Override
            public ClassNode resolveClassNode(String binaryName) throws ClassResolutionException {
                return cache.computeIfAbsent(binaryName, s -> {
                    ClassNode node = new ClassNode();
                    resolveClassReader(s).accept(node, 0);
                    return node;
                });
            }
        };
    }
}
