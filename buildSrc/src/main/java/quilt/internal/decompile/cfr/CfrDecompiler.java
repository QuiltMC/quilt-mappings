package quilt.internal.decompile.cfr;

import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.api.ClassFileSource;
import org.benf.cfr.reader.api.OutputSinkFactory;
import org.benf.cfr.reader.api.SinkReturns;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.gradle.api.Project;
import quilt.internal.decompile.AbstractDecompiler;
import quilt.internal.decompile.javadoc.ClassJavadocProvider;
import quilt.internal.decompile.javadoc.FieldJavadocProvider;
import quilt.internal.decompile.javadoc.MethodJavadocProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CfrDecompiler extends AbstractDecompiler {
    private ClassJavadocProvider classJavadocProvider;
    private FieldJavadocProvider fieldJavadocProvider;
    private MethodJavadocProvider methodJavadocProvider;

    public CfrDecompiler(Project project) {
        super(project);
    }

    @Override
    public void decompile(File file, File outputDir, Map<String, Object> optionsMap, Collection<File> libraries) {
        Map<String, String> stringOptions = optionsMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
        Options options = OptionsImpl.getFactory().create(stringOptions);

        try (ZipFile zipFile = new ZipFile(file)) {
            ClassFileSource classFileSource = new ClassFileSource() {
                private final Map<String, byte[]> classFiles = new HashMap<>();

                @Override
                public void informAnalysisRelativePathDetail(String usePath, String classFilePath) {
                }

                @Override
                public Collection<String> addJar(String jarPath) {
                    List<String> contents = new ArrayList<>();
                    File file = new File(jarPath);
                    try (ZipFile zipFile = new ZipFile(file)) {
                        List<? extends ZipEntry> entries = Collections.list(zipFile.entries());
                        for (ZipEntry entry : entries) {
                            contents.add(entry.getName());
                            try (InputStream inputStream = zipFile.getInputStream(entry)) {
                                classFiles.put(entry.getName(), inputStream.readAllBytes());
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to add jar " + jarPath, e);
                    }

                    return contents;
                }

                @Override
                public String getPossiblyRenamedPath(String path) {
                    return path;
                }

                @Override
                public Pair<byte[], String> getClassFileContent(String path) {
                    if (classFiles.containsKey(path)) {
                        return new Pair<>(classFiles.get(path), path);
                    }
                    return null;
                }
            };

            for (File library : libraries) {
                classFileSource.addJar(library.getAbsolutePath());
            }
            classFileSource.addJar(file.getAbsolutePath());

            // TODO: Replace this with something that allows using a DCCommonState
            CfrDriver driver = new CfrDriver.Builder()
                    .withBuiltOptions(options)
                    .withClassFileSource(classFileSource)
                    .withOutputSink(new OutputSinkFactory() {
                        @Override
                        public List<SinkClass> getSupportedSinks(SinkType sinkType, Collection<SinkClass> available) {
                            return switch (sinkType) {
                                case JAVA -> List.of(SinkClass.DECOMPILED);
                                case EXCEPTION -> List.of(SinkClass.STRING, SinkClass.EXCEPTION_MESSAGE);
                                case PROGRESS -> List.of(SinkClass.STRING);
                                default -> Collections.emptyList();
                            };
                        }

                        @Override
                        public <T> Sink<T> getSink(SinkType sinkType, SinkClass sinkClass) {
                            return switch (sinkType) {
                                case JAVA -> (d -> writeDecompiled((SinkReturns.Decompiled) d, outputDir));
                                case EXCEPTION -> (e -> getProject().getLogger().error((String) e));
                                case PROGRESS -> (p -> getProject().getLogger().debug((String) p));
                                default -> null;
                            };
                        }
                    })
                    .build();

            List<String> classFiles = Collections.list(zipFile.entries()).stream().map(ZipEntry::getName).filter(s -> s.endsWith(".class")).collect(Collectors.toList());

            driver.analyse(classFiles);
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompile " + file, e);
        }
    }

    private boolean hasMemberJavadocProvider() {
        return this.classJavadocProvider != null || this.fieldJavadocProvider != null || this.methodJavadocProvider != null;
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

    private void writeDecompiled(SinkReturns.Decompiled decompiled, File outputDir) {
        String packageName = decompiled.getPackageName().replace(".", "/");
        if (!packageName.isEmpty()) {
            packageName += "/";
        }

        String fileName = decompiled.getClassName() + ".java";
        String path = packageName + fileName;

        try {
            Path outputFile = outputDir.toPath().resolve(path);
            if (!Files.exists(outputFile.getParent())) {
                Files.createDirectories(outputFile.getParent());
            }

            Files.writeString(outputFile, decompiled.getJava());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write decompiled file " + path, e);
        }
    }
}
