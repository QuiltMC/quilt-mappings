package quilt.internal.tasks.build;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.jarmapping.MapPerVersionMappingsJarTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class GeneratePackageInfoMappingsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "generatePackageInfoMappings";

    // TODO this should not be separate from outputDir
    private final File mappings = this.getProject().file("mappings");
    // TODO this should be an input
    private final File inputJar = this.fileConstants.perVersionMappingsJar;

    public GeneratePackageInfoMappingsTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        this.dependsOn(MapPerVersionMappingsJarTask.TASK_NAME);
    }

    @Input
    public abstract Property<String> getPackageName();

    // TODO this should be an output mapped from mappings
    private Path getOutputDir() {
        return this.mappings.toPath().resolve(this.getPackageName().get());
    }

    @TaskAction
    public void generate() throws IOException {
        // TODO eliminate project access in task action
        this.getProject().getLogger().lifecycle("Scanning {} for package-info classes", this.inputJar);

        if (Files.exists(this.getOutputDir())) {
            try (Stream<Path> filePaths = Files.walk(this.getOutputDir()).filter(Files::isRegularFile)) {
                final List<Path> contents = filePaths.toList();
                for (int i = contents.size() - 1; i >= 0; i--) {
                    Files.delete(contents.get(i));
                }

                Files.delete(this.getOutputDir());
            }
        }

        try (ZipFile zipFile = new ZipFile(this.inputJar)) {
            final List<? extends ZipEntry> entries = Collections.list(zipFile.entries());

            for (final ZipEntry entry : entries) {
                if (entry.getName().endsWith(".class")) {
                    try (InputStream stream = zipFile.getInputStream(entry)) {
                        this.processEntry(entry.getName(), stream);
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

        final ClassReader classReader = new ClassReader(inputStream);
        final ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);

        if (classNode.access != (Opcodes.ACC_ABSTRACT | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_INTERFACE)) {
            // We only care about abstract synthetic interfaces, hopefully this is specific enough
            return;
        }

        if (!classNode.methods.isEmpty() || !classNode.fields.isEmpty() || !classNode.interfaces.isEmpty()) {
            // Nope cannot be a package-info
            return;
        }

        this.generateMapping(name);
    }

    private void generateMapping(String name) throws IOException {
        String packageInfoId = name.substring(name.lastIndexOf("_") + 1);

        if (Character.isLowerCase(packageInfoId.charAt(0))) {
            packageInfoId = packageInfoId.substring(0, 1).toUpperCase(Locale.ROOT) + packageInfoId.substring(1);
        }

        final String className = "PackageInfo" + packageInfoId;
        final String fullName = this.getPackageName().get() + className;
        final Path mappingsFile = this.getOutputDir().resolve(className + ".mapping");

        if (!Files.exists(mappingsFile.getParent())) {
            Files.createDirectories(mappingsFile.getParent());
        }

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(mappingsFile))) {
            writer.printf("CLASS %s %s", name, fullName);
            // println is platform-dependent and may produce CRLF.
            writer.print('\n');
        }
    }
}
