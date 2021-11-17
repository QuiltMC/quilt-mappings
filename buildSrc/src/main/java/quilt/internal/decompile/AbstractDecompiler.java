package quilt.internal.decompile;

import org.gradle.api.Project;
import quilt.internal.decompile.javadoc.ClassJavadocProvider;
import quilt.internal.decompile.javadoc.FieldJavadocProvider;
import quilt.internal.decompile.javadoc.MethodJavadocProvider;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

public abstract class AbstractDecompiler {
    private final Project project;

    public AbstractDecompiler(Project project) {
        this.project = project;
    }

    public void decompile(Path file, Path outputDir, Map<String, Object> options, Collection<File> libraries) {
        decompile(file.toFile(), outputDir.toFile(), options, libraries);
    }

    public abstract void decompile(File file, File outputDir, Map<String, Object> options, Collection<File> libraries);

    protected Project getProject() {
        return project;
    }

    public void withClassJavadocProvider(ClassJavadocProvider javadocProvider) {
    }

    public void withFieldJavadocProvider(FieldJavadocProvider javadocProvider) {
    }

    public void withMethodJavadocProvider(MethodJavadocProvider javadocProvider) {
    }
}
