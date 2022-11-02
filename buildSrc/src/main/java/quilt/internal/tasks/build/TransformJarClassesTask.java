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

public class TransformJarClassesTask extends DefaultMappingsTask {
    private final RegularFileProperty jarFile;
    private final DirectoryProperty output;
    private final List<VisitorFactory> visitorFactories = new ArrayList<>();
    private final List<Predicate<ClassNode>> filters = new ArrayList<>();

    public TransformJarClassesTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        jarFile = getProject().getObjects().fileProperty();
        output = getProject().getObjects().directoryProperty();
    }

    @InputFile
    public RegularFileProperty getJarFile() {
        return jarFile;
    }

    @OutputDirectory
    public DirectoryProperty getOutput() {
        return output;
    }

    public void visitor(VisitorFactory visitorFactory) {
        visitorFactories.add(visitorFactory);
    }

    public void filter(Predicate<ClassNode> filter) {
        filters.add(filter);
    }

    @TaskAction
    public void transform() throws IOException {
        Map<String, byte[]> classFiles = new HashMap<>();
        try (ZipFile zipFile = new ZipFile(jarFile.getAsFile().get())) {
            List<? extends ZipEntry> entries = Collections.list(zipFile.entries());
            for (ZipEntry entry : entries) {
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    try (InputStream stream = zipFile.getInputStream(entry)) {
                        classFiles.put(name, stream.readAllBytes());
                    }
                }
            }
        }

        Predicate<ClassNode> filter = filters.stream().reduce(Predicate::and).orElse(node -> true);

        Map<String, byte[]> transformedClassFiles = new HashMap<>();
        for (String name : classFiles.keySet()) {
            ClassReader reader = new ClassReader(classFiles.get(name));
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            ClassVisitor visitor = writer;
            for (VisitorFactory visitorFactory : visitorFactories) {
                visitor = visitorFactory.create(visitor);
            }

            if (!(visitor instanceof ClassNode)) {
                visitor = new ForwardingClassNode(visitor);
            }
            ClassNode node = (ClassNode) visitor;

            reader.accept(visitor, 0);
            if (filter.test(node)) {
                transformedClassFiles.put(name, writer.toByteArray());
            }
        }

        // Ensure the output directory is empty
        File outputFile = output.getAsFile().get();
        FileUtils.deleteDirectory(outputFile);
        Path outputPath = outputFile.toPath();

        for (String name : transformedClassFiles.keySet()) {
            Path path = outputPath.resolve(name);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            Files.write(path, transformedClassFiles.get(name));
        }
    }

    interface VisitorFactory {
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
            this.accept(visitor);
        }
    }
}
