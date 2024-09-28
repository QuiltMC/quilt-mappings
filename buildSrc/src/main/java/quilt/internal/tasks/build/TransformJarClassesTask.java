package quilt.internal.tasks.build;

import org.apache.commons.io.FileUtils;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class TransformJarClassesTask extends DefaultMappingsTask {
    private final List<VisitorFactory> visitorFactories = new ArrayList<>();
    private final List<Predicate<ClassNode>> filters = new ArrayList<>();

    @InputFile
    public abstract RegularFileProperty getJarFile();

    @OutputDirectory
    public abstract DirectoryProperty getOutput();

    public TransformJarClassesTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
    }

    public void visitor(VisitorFactory visitorFactory) {
        this.visitorFactories.add(visitorFactory);
    }

    public void filter(Predicate<ClassNode> filter) {
        this.filters.add(filter);
    }

    @TaskAction
    public void transform() throws IOException {
        final Map<String, byte[]> classFiles = new HashMap<>();
        try (ZipFile zipFile = new ZipFile(this.getJarFile().getAsFile().get())) {
            final List<? extends ZipEntry> entries = Collections.list(zipFile.entries());
            for (final ZipEntry entry : entries) {
                final String name = entry.getName();
                if (name.endsWith(".class")) {
                    try (InputStream stream = zipFile.getInputStream(entry)) {
                        classFiles.put(name, stream.readAllBytes());
                    }
                }
            }
        }

        final Predicate<ClassNode> filter = this.filters.stream().reduce(Predicate::and).orElse(node -> true);

        final Map<String, byte[]> transformedClassFiles = new HashMap<>();
        for (final String name : classFiles.keySet()) {
            final ClassReader reader = new ClassReader(classFiles.get(name));
            final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            ClassVisitor visitor = writer;
            for (final VisitorFactory visitorFactory : this.visitorFactories) {
                visitor = visitorFactory.create(visitor);
            }

            if (!(visitor instanceof ClassNode)) {
                visitor = new ForwardingClassNode(visitor);
            }
            final ClassNode node = (ClassNode) visitor;

            reader.accept(visitor, 0);
            if (filter.test(node)) {
                transformedClassFiles.put(name, writer.toByteArray());
            }
        }

        // Ensure the output directory is empty
        final File outputFile = this.getOutput().getAsFile().get();
        FileUtils.deleteDirectory(outputFile);
        final Path outputPath = outputFile.toPath();

        for (final String name : transformedClassFiles.keySet()) {
            final Path path = outputPath.resolve(name);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            Files.write(path, transformedClassFiles.get(name));
        }
    }

    public interface VisitorFactory {
        ClassVisitor create(ClassVisitor visitor);
    }

    private static class ForwardingClassNode extends ClassNode {
        private final ClassVisitor visitor;

        public ForwardingClassNode(ClassVisitor visitor) {
            super(Opcodes.ASM9);
            this.visitor = visitor;
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            this.accept(this.visitor);
        }
    }
}
