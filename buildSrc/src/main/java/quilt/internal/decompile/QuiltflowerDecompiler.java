package quilt.internal.decompile;

import net.fabricmc.fernflower.api.IFabricJavadocProvider;
import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;
import org.jetbrains.java.decompiler.main.decompiler.BaseDecompiler;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMethod;
import quilt.internal.decompile.javadoc.ClassJavadocProvider;
import quilt.internal.decompile.javadoc.FieldJavadocProvider;
import quilt.internal.decompile.javadoc.MethodJavadocProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class QuiltflowerDecompiler extends AbstractDecompiler implements IBytecodeProvider {
    private IFabricJavadocProvider javadocProvider;
    private ClassJavadocProvider classJavadocProvider;
    private FieldJavadocProvider fieldJavadocProvider;
    private MethodJavadocProvider methodJavadocProvider;

    public QuiltflowerDecompiler(Project project) {
        super(project);
    }

    @Override
    public void decompile(File file, File outputDir, Map<String, Object> options, Collection<File> libraries) {
        Path outputPath = outputDir.toPath();

        IFabricJavadocProvider javadocProvider = null;
        if (this.javadocProvider != null) {
            javadocProvider = this.javadocProvider;
        } else if (hasMemberJavadocProvider()) {
            javadocProvider = new IFabricJavadocProvider() {
                @Override
                public String getClassDoc(StructClass structClass) {
                    if (classJavadocProvider != null) {
                        return classJavadocProvider.provide(structClass.qualifiedName, structClass.getAccessFlags());
                    }

                    return null;
                }

                @Override
                public String getFieldDoc(StructClass structClass, StructField structField) {
                    if (fieldJavadocProvider != null) {
                        return fieldJavadocProvider.provide(structField.getName(), structField.getDescriptor(), structClass.qualifiedName, structField.getAccessFlags());
                    }

                    return null;
                }

                @Override
                public String getMethodDoc(StructClass structClass, StructMethod structMethod) {
                    if (methodJavadocProvider != null) {
                        return methodJavadocProvider.provide(structMethod.getName(), structMethod.getDescriptor(), structClass.qualifiedName, structMethod.getAccessFlags());
                    }

                    return null;
                }
            };
        }

        if (javadocProvider != null) {
            options.put(IFabricJavadocProvider.PROPERTY_NAME, javadocProvider);
        }

        IResultSaver resultSaver = new IResultSaver() {
            @Override
            public void saveFolder(String path) {
                Path folder = outputPath.resolve(path);
                if (!Files.exists(folder)) {
                    try {
                        Files.createDirectories(folder);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to create directory " + folder, e);
                    }
                }
            }

            @Override
            public void copyFile(String source, String path, String entryName) {
            }

            @Override
            public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
                Path file = outputPath.resolve(path).resolve(entryName);
                try {
                    if (!Files.exists(file.getParent())) {
                        Files.createDirectories(file.getParent());
                    }

                    Files.writeString(file, content);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write class file " + file, e);
                }
            }

            @Override
            public void createArchive(String path, String archiveName, Manifest manifest) {
            }

            @Override
            public void saveDirEntry(String path, String archiveName, String entryName) {
            }

            @Override
            public void copyEntry(String source, String path, String archiveName, String entry) {
            }

            @Override
            public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
                saveClassFile(path, qualifiedName, entryName, content, null);
            }

            @Override
            public void closeArchive(String path, String archiveName) {
            }
        };

        BaseDecompiler decompiler = new BaseDecompiler(this, resultSaver, options, new LoggerImpl());

        decompiler.addSource(file);
        for (File library : libraries) {
            decompiler.addLibrary(library);
        }

        decompiler.decompileContext();
    }

    @Override
    public byte[] getBytecode(String externalPath, String internalPath) throws IOException {
        File file = new File(externalPath);
        if (internalPath == null) {
            try (InputStream stream = new FileInputStream(file)) {
                return stream.readAllBytes();
            }
        } else {
            try (ZipFile zipFile = new ZipFile(file)) {
                ZipEntry entry = zipFile.getEntry(internalPath);
                if (entry == null) {
                    throw new FileNotFoundException("Entry " + internalPath + " not found in " + file);
                }

                try (InputStream stream = zipFile.getInputStream(entry)) {
                    return stream.readAllBytes();
                }
            }
        }
    }

    private boolean hasMemberJavadocProvider() {
        return this.classJavadocProvider != null || this.fieldJavadocProvider != null || this.methodJavadocProvider != null;
    }

    public void withFabricJavadocProvider(IFabricJavadocProvider javadocProvider) {
        this.javadocProvider = javadocProvider;
    }

    @Override
    public void withClassJavadocProvider(ClassJavadocProvider javadocProvider) {
        this.classJavadocProvider = javadocProvider;
    }

    @Override
    public void withFieldJavadocProvider(FieldJavadocProvider javadocProvider) {
        this.fieldJavadocProvider = javadocProvider;
    }

    @Override
    public void withMethodJavadocProvider(MethodJavadocProvider javadocProvider) {
        this.methodJavadocProvider = javadocProvider;
    }

    private class LoggerImpl extends IFernflowerLogger {
        private static LogLevel getLogLevel(Severity severity) {
            return switch (severity) {
                case TRACE -> LogLevel.DEBUG;
                case INFO -> LogLevel.INFO;
                case WARN -> LogLevel.WARN;
                case ERROR -> LogLevel.ERROR;
            };
        }

        @Override
        public void writeMessage(String message, Severity severity) {
            getProject().getLogger().log(getLogLevel(severity), message);
        }

        @Override
        public void writeMessage(String message, Severity severity, Throwable t) {
            getProject().getLogger().log(getLogLevel(severity), message, t);
        }
    }
}
