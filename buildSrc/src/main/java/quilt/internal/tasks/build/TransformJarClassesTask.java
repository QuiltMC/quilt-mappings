package quilt.internal.tasks.build;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.quiltmc.draftsman.asm.DraftsmanClassTransformer;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TransformJarClassesTask extends DefaultMappingsTask {
    private final RegularFileProperty jarFile;
    private final DirectoryProperty output;
    private final List<VisitorFactory> visitorFactories = new ArrayList<>();

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

        Map<String, byte[]> transformedClassFiles = new HashMap<>();
        for (String name : classFiles.keySet()) {
            ClassReader reader = new ClassReader(classFiles.get(name));
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            ClassVisitor visitor = writer;
            for (VisitorFactory visitorFactory : visitorFactories) {
                visitor = visitorFactory.create(visitor);
            }

            reader.accept(visitor, 0);
            transformedClassFiles.put(name, writer.toByteArray());
        }

        for (String name : transformedClassFiles.keySet()) {
            Path path = output.getAsFile().get().toPath().resolve(name);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            Files.write(path, transformedClassFiles.get(name));
        }
    }

    interface VisitorFactory {
        ClassVisitor create(ClassVisitor visitor);
    }
}
