package quilt.internal.util;

import net.fabricmc.mappingio.format.Tiny2Reader;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import quilt.internal.decompile.javadoc.ClassJavadocProvider;
import quilt.internal.decompile.javadoc.FieldJavadocProvider;
import quilt.internal.decompile.javadoc.MethodJavadocProvider;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;

public class MappingsJavadocProvider implements ClassJavadocProvider, FieldJavadocProvider, MethodJavadocProvider {
    private final MemoryMappingTree tree = new MemoryMappingTree();
    private final int namespaceId;

    public MappingsJavadocProvider(File mappingsFile, String namespace) throws IOException {
        try (Reader reader = Files.newBufferedReader(mappingsFile.toPath())) {
            Tiny2Reader.read(reader, tree);
        }
        namespaceId = tree.getNamespaceId(namespace);
    }

    @Override
    public String provideClassJavadoc(String className, boolean isRecord) {
        MappingTree.ClassMapping mapping = tree.getClass(className, namespaceId);
        if (mapping != null) {
            return mapping.getComment();
        }

        return null;
    }

    @Override
    public String provideFieldJavadoc(String fieldName, String descriptor, String owner) {
        MappingTree.ClassMapping ownerMapping = tree.getClass(owner, namespaceId);
        if (ownerMapping != null) {
            MappingTree.FieldMapping fieldMapping = ownerMapping.getField(fieldName, descriptor, namespaceId);
            if (fieldMapping != null) {
                return fieldMapping.getComment();
            }
        }

        return null;
    }

    @Override
    public String provideMethodJavadoc(String methodName, String descriptor, String owner) {
        MappingTree.ClassMapping ownerMapping = tree.getClass(owner, namespaceId);
        if (ownerMapping != null) {
            MappingTree.MethodMapping methodMapping = ownerMapping.getMethod(methodName, descriptor, namespaceId);
            if (methodMapping != null) {
                StringBuilder argComments = new StringBuilder();
                for (MappingTree.MethodArgMapping argMapping : methodMapping.getArgs()) {
                    if (argMapping.getComment() != null) {
                        argComments.append("@param ").append(argMapping.getName(namespaceId)).append(" ").append(argMapping.getComment());
                        argComments.append("\n");
                    }
                }

                if (!argComments.isEmpty()) {
                    return methodMapping.getComment() + "\n" + argComments;
                }

                return methodMapping.getComment();
            }
        }

        return null;
    }
}
