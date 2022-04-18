package quilt.internal.tasks.decompile;

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.decompile.AbstractDecompiler;
import quilt.internal.decompile.Decompilers;
import quilt.internal.decompile.javadoc.ClassJavadocProvider;
import quilt.internal.decompile.javadoc.FieldJavadocProvider;
import quilt.internal.decompile.javadoc.MethodJavadocProvider;
import quilt.internal.tasks.DefaultMappingsTask;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DecompileTask extends DefaultMappingsTask {
    private final Property<Decompilers> decompiler;
    private Map<String, Object> decompilerOptions;
    private final RegularFileProperty input;
    private final RegularFileProperty output;
    private final Property<FileCollection> libraries;
    private ClassJavadocProvider classJavadocProvider;
    private FieldJavadocProvider fieldJavadocProvider;
    private MethodJavadocProvider methodJavadocProvider;

    public DecompileTask() {
        super(Constants.Groups.DECOMPILE_GROUP);
        input = getProject().getObjects().fileProperty();
        output = getProject().getObjects().fileProperty();
        decompiler = getProject().getObjects().property(Decompilers.class);
        libraries = getProject().getObjects().property(FileCollection.class);
    }

    @TaskAction
    public void decompile() {
        Map<String, Object> options = decompilerOptions == null ? new HashMap<>() : decompilerOptions;
        Collection<File> libraries = this.libraries.getOrNull() == null ? Collections.emptyList() : this.libraries.get().getFiles();

        AbstractDecompiler decompiler = getAbstractDecompiler();

        if (classJavadocProvider != null) {
            decompiler.withClassJavadocProvider(classJavadocProvider);
        }
        if (fieldJavadocProvider != null) {
            decompiler.withFieldJavadocProvider(fieldJavadocProvider);
        }
        if (methodJavadocProvider != null) {
            decompiler.withMethodJavadocProvider(methodJavadocProvider);
        }

        decompiler.decompile(getInput().getAsFile().get(), getOutput().getAsFile().get(), options, libraries);
    }

    @Internal
    public AbstractDecompiler getAbstractDecompiler() {
        return decompiler.get().getProvider().provide(getProject());
    }

    @Input
    public Property<Decompilers> getDecompiler() {
        return decompiler;
    }

    @Optional
    @Input
    public Map<String, Object> getDecompilerOptions() {
        return decompilerOptions;
    }

    @Optional
    @Input
    public Property<FileCollection> getLibraries() {
        return libraries;
    }

    public void setDecompilerOptions(Map<String, Object> decompilerOptions) {
        this.decompilerOptions = decompilerOptions;
    }

    @InputFiles
    public RegularFileProperty getInput() {
        return input;
    }

    @OutputDirectory
    public RegularFileProperty getOutput() {
        return output;
    }

    public void classJavadocProvider(ClassJavadocProvider provider) {
        classJavadocProvider = provider;
    }

    public void fieldJavadocProvider(FieldJavadocProvider provider) {
        fieldJavadocProvider = provider;
    }

    public void methodJavadocProvider(MethodJavadocProvider provider) {
        methodJavadocProvider = provider;
    }
}
