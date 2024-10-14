package quilt.internal.decompile.javadoc;

import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

import net.fabricmc.mappingio.format.tiny.Tiny2FileReader;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;

public class MappingsJavadocProvider implements UniversalJavadocProvider {
    private transient final MemoryMappingTree tree = new MemoryMappingTree();
    private final int namespaceId;

    public MappingsJavadocProvider(File mappingsFile, String namespace) throws IOException {
        try (Reader reader = Files.newBufferedReader(mappingsFile.toPath())) {
            Tiny2FileReader.read(reader, this.tree);
        }
        this.namespaceId = this.tree.getNamespaceId(namespace);
    }

    @Override
    public String provideClassJavadoc(String className, boolean isRecord) {
        final MappingTree.ClassMapping mapping = this.tree.getClass(className, this.namespaceId);
        if (mapping != null) {
            final StringBuilder javadoc;
            if (mapping.getComment() != null) {
                javadoc = new StringBuilder(mapping.getComment());
                javadoc.append("\n\n");
            } else {
                javadoc = new StringBuilder();
            }

            // Add mapping info
            for (int i = tree.getMinNamespaceId(); i < tree.getMaxNamespaceId(); i++) {
                String namespace = tree.getNamespaceName(i);
                String name = mapping.getName(namespace);
                javadoc.append("@mapping {@literal ");
                javadoc.append(namespace).append(" ").append(name).append("}");
                javadoc.append("\n");
            }

            return javadoc.toString().trim();
        }

        return "Mapping not found";
    }

    @Override
    public String provideFieldJavadoc(String fieldName, String descriptor, String owner) {
        final MappingTree.ClassMapping ownerMapping = this.tree.getClass(owner, this.namespaceId);
        if (ownerMapping != null) {
            final MappingTree.FieldMapping fieldMapping =
                ownerMapping.getField(fieldName, descriptor, this.namespaceId);
            if (fieldMapping != null) {
                final StringBuilder javadoc;
                if (fieldMapping.getComment() != null) {
                    javadoc = new StringBuilder(fieldMapping.getComment());
                    javadoc.append("\n\n");
                } else {
                    javadoc = new StringBuilder();
                }

                // Add mapping info
                for (int i = this.tree.getMinNamespaceId(); i < this.tree.getMaxNamespaceId(); i++) {
                    final String namespace = this.tree.getNamespaceName(i);
                    final String name = fieldMapping.getName(namespace);
                    final String selector =
                        "L" + ownerMapping.getName(namespace) + ";" + name + ":" + fieldMapping.getDesc(namespace);
                    javadoc.append("@mapping {@literal ");
                    javadoc.append(namespace).append(" ").append(name).append(" ").append(selector).append("}");
                    javadoc.append("\n");
                }

                return javadoc.toString().trim();
            }
        }

        return "Mapping not found";
    }

    @Override
    public String provideMethodJavadoc(String methodName, String descriptor, String owner) {
        final MappingTree.ClassMapping ownerMapping = this.tree.getClass(owner, this.namespaceId);
        if (ownerMapping != null) {
            final MappingTree.MethodMapping methodMapping =
                ownerMapping.getMethod(methodName, descriptor, this.namespaceId);
            if (methodMapping != null) {
                final StringBuilder javadoc;
                if (methodMapping.getComment() != null) {
                    javadoc = new StringBuilder(methodMapping.getComment());
                    javadoc.append("\n\n");
                } else {
                    javadoc = new StringBuilder();
                }

                // Generate @param tags
                final StringBuilder argComments = new StringBuilder();
                for (MappingTree.MethodArgMapping argMapping : methodMapping.getArgs()) {
                    if (argMapping.getComment() != null) {
                        argComments.append("@param ")
                            .append(argMapping.getName(this.namespaceId)).append(" ")
                            .append(argMapping.getComment());
                        argComments.append("\n");
                    }
                }

                if (!argComments.isEmpty()) {
                    javadoc.append(argComments);
                    javadoc.append("\n");
                }

                // Add mapping info
                for (int i = this.tree.getMinNamespaceId(); i < this.tree.getMaxNamespaceId(); i++) {
                    final String namespace = this.tree.getNamespaceName(i);
                    final String name = methodMapping.getName(namespace);
                    final String selector = "L" + ownerMapping.getName(namespace) + ";" + name + methodMapping.getDesc(namespace);
                    javadoc.append("@mapping {@literal ");
                    javadoc.append(namespace).append(" ").append(name).append(" ").append(selector).append("}");
                    javadoc.append("\n");
                }

                return javadoc.toString().trim();
            }
        }

        return "Mapping not found";
    }

    public static abstract class Source implements ValueSource<MappingsJavadocProvider, Source.Params> {
        @Override
        public MappingsJavadocProvider obtain() {
            final Params params = this.getParameters();

            try {
                return new MappingsJavadocProvider(
                    params.getMappingsFile().get().getAsFile(),
                    params.getNamespace().get()
                );
            } catch (IOException e) {
                throw new GradleException("Failed to create javadoc provider", e);
            }
        }

        public interface Params extends ValueSourceParameters {
            RegularFileProperty getMappingsFile();

            Property<String> getNamespace();
        }
    }
}
