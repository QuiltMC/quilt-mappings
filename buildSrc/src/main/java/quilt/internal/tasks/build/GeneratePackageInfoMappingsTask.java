package quilt.internal.tasks.build;

import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.mappings.MappingsDirOutputtingTask;

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

public abstract class GeneratePackageInfoMappingsTask extends DefaultMappingsTask implements MappingsDirOutputtingTask {
    public static final String TASK_NAME = "generatePackageInfoMappings";

    private static void processEntry(String name, InputStream inputStream, String packageName, Path outputDir) throws IOException {
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

        generateMapping(name, packageName, outputDir);
    }

    private static void generateMapping(String name, String packageName, Path outputDir) throws IOException {
        String packageInfoId = name.substring(name.lastIndexOf("_") + 1);

        if (Character.isLowerCase(packageInfoId.charAt(0))) {
            packageInfoId = packageInfoId.substring(0, 1).toUpperCase(Locale.ROOT) + packageInfoId.substring(1);
        }

        final String className = "PackageInfo" + packageInfoId;
        final String fullName = packageName + className;
        final Path mappingsFile = outputDir.resolve(className + ".mapping");

        if (!Files.exists(mappingsFile.getParent())) {
            Files.createDirectories(mappingsFile.getParent());
        }

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(mappingsFile))) {
            writer.printf("CLASS %s %s", name, fullName);
            // println is platform-dependent and may produce CRLF.
            writer.print('\n');
        }
    }

    // TODO this should not be separate from outputDir
    // private final File mappings = this.getProject().file("mappings");
    // TODO this should be an input
    // private final File inputJar = this.fileConstants.perVersionMappingsJar;

    @InputFile
    public abstract RegularFileProperty getInputJar();

    @Input
    public abstract Property<String> getPackageName();

    // TODO this should be an output mapped from mappings
    // private Path getOutputDir() {
    //     return this.mappings.toPath().resolve(this.getPackageName().get());
    // }

    @OutputDirectory
    protected abstract DirectoryProperty getOutputDir();

    public GeneratePackageInfoMappingsTask() {
        super(Constants.Groups.BUILD_MAPPINGS);

        this.getOutputDir().convention(this.getMappingsDir().zip(this.getPackageName(), Directory::dir));
    }

    @TaskAction
    public void generate() throws IOException {
        final File inputJar = this.getInputJar().get().getAsFile();

        this.getLogger().lifecycle("Scanning {} for package-info classes", inputJar);

        final Path outputDir = this.getOutputDir().get().getAsFile().toPath();
        if (Files.exists(outputDir)) {
            try (Stream<Path> filePaths = Files.walk(outputDir).filter(Files::isRegularFile)) {
                final List<Path> contents = filePaths.toList();
                for (int i = contents.size() - 1; i >= 0; i--) {
                    Files.delete(contents.get(i));
                }

                Files.delete(outputDir);
            }
        }

        try (ZipFile zipFile = new ZipFile(inputJar)) {
            final List<? extends ZipEntry> entries = Collections.list(zipFile.entries());

            for (final ZipEntry entry : entries) {
                if (entry.getName().endsWith(".class")) {
                    try (InputStream stream = zipFile.getInputStream(entry)) {
                        processEntry(entry.getName(), stream, this.getPackageName().get(), outputDir);
                    }
                }
            }
        }
    }
}
