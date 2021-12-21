package quilt.internal.tasks.build;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.quiltmc.draftsman.asm.DraftsmanClassTransformer;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EraseBytecodeTask extends DefaultMappingsTask {
    private final RegularFileProperty jarFile;
    private final DirectoryProperty output;

    public EraseBytecodeTask() {
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

    @TaskAction
    public void erase() throws IOException {
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
            DraftsmanClassTransformer transformer = new DraftsmanClassTransformer(classFiles.get(name), false);
            byte[] transformed = transformer.transform();
            transformedClassFiles.put(name, transformed);
        }

        for (String name : transformedClassFiles.keySet()) {
            Path path = output.getAsFile().get().toPath().resolve(name);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            Files.write(path, transformedClassFiles.get(name));
        }
    }
}
