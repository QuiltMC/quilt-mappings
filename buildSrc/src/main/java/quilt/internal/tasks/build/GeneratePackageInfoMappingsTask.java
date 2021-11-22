package quilt.internal.tasks.build;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.jarmapping.MapPerVersionMappingsJarTask;
import quilt.internal.tasks.setup.DownloadPerVersionMappingsTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GeneratePackageInfoMappingsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "generatePackageInfoMappings";
    private final Property<String> packageName = getProject().getObjects().property(String.class);
    private final File mappings = getProject().file("mappings");
    private final File inputJar = fileConstants.perVersionMappingsJar;

    public GeneratePackageInfoMappingsTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        this.dependsOn(MapPerVersionMappingsJarTask.TASK_NAME);
    }

    @Input
    public Property<String> getPackageName() {
        return packageName;
    }

    private Path getOutputDir() {
        return mappings.toPath().resolve(packageName.get());
    }

    @TaskAction
    public void generate() throws IOException {
        getProject().getLogger().lifecycle("Scanning {} for package-info classes", inputJar);

        if (Files.exists(getOutputDir())) {
            List<Path> contents = Files.walk(getOutputDir()).filter(Files::isRegularFile).collect(Collectors.toList());
            for (int i = contents.size() - 1; i >= 0; i--) {
                Files.delete(contents.get(i));
            }
            Files.delete(getOutputDir());
        }

        try (ZipFile zipFile = new ZipFile(inputJar)) {
            List<? extends ZipEntry> entries = Collections.list(zipFile.entries());

            for (ZipEntry entry : entries) {
                if (entry.getName().endsWith(".class")) {
                    try (InputStream stream = zipFile.getInputStream(entry)) {
                        processEntry(entry.getName(), stream);
                    }
                }
            }
        }
    }

    private void processEntry(String name, InputStream inputStream) throws IOException {
        name = name.replace(".class", "");

        if (name.contains("$")) {
            // Dont care about inner classes
            return;
        }

        ClassReader classReader = new ClassReader(inputStream);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);

        if (classNode.access != (Opcodes.ACC_ABSTRACT | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_INTERFACE)) {
            // We only care about abstract synthetic interfaces, hopefully this is specific enough
            return;
        }

        if (classNode.methods.size() > 0 || classNode.fields.size() > 0 || classNode.interfaces.size() > 0) {
            // Nope cannot be a package-info
            return;
        }

        generateMapping(name);
    }

    private void generateMapping(String name) throws IOException {
        String packageInfoId = name.substring(name.lastIndexOf("_") + 1);

        if (Character.isLowerCase(packageInfoId.charAt(0))) {
            packageInfoId = packageInfoId.substring(0, 1).toUpperCase(Locale.ROOT) + packageInfoId.substring(1);
        }

        String className = "PackageInfo" + packageInfoId;
        String fullName = packageName.get() + className;
        Path mappingsFile = getOutputDir().resolve(className + ".mapping");

        if (!Files.exists(mappingsFile.getParent())) {
            Files.createDirectories(mappingsFile.getParent());
        }

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(mappingsFile))) {
            writer.printf("CLASS %s %s", name, fullName);
            writer.print('\n'); // println is platform-dependent and may produce CRLF.
        }
    }
}
