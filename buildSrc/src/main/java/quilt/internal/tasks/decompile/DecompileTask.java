package quilt.internal.tasks.decompile;

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
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

public abstract class DecompileTask extends DefaultMappingsTask {
    @Input
    public abstract Property<Decompilers> getDecompiler();

    @Optional
    @Input
    public abstract MapProperty<String, Object> getDecompilerOptions();

    @Optional
    @Input
    public abstract Property<FileCollection> getLibraries();

    @InputFiles
    public abstract RegularFileProperty getInput();

    @OutputDirectory
    public abstract RegularFileProperty getOutput();

    // TODO see if all three of these can be optional inputs
    private ClassJavadocProvider classJavadocProvider;
    private FieldJavadocProvider fieldJavadocProvider;
    private MethodJavadocProvider methodJavadocProvider;

    public DecompileTask() {
        super(Constants.Groups.DECOMPILE_GROUP);
    }

    @TaskAction
    public void decompile() {
        // mapping to HashMap is required; the unmapped Map is unmodifiable and VineflowerDecompiler needs to modify it
        // this issue occurred for decompileTargetVineflower
        final Map<String, Object> options = this.getDecompilerOptions().map(HashMap::new).getOrElse(new HashMap<>());

        final Collection<File> libraries = this.getLibraries().<Collection<File>>map(FileCollection::getFiles)
            .getOrElse(Collections.emptyList());

        final AbstractDecompiler decompiler = this.getAbstractDecompiler();

        if (this.classJavadocProvider != null) {
            decompiler.withClassJavadocProvider(this.classJavadocProvider);
        }
        if (this.fieldJavadocProvider != null) {
            decompiler.withFieldJavadocProvider(this.fieldJavadocProvider);
        }
        if (this.methodJavadocProvider != null) {
            decompiler.withMethodJavadocProvider(this.methodJavadocProvider);
        }

        decompiler.decompile(this.getInput().getAsFile().get(), this.getOutput().getAsFile().get(), options, libraries);
    }

    // TODO see if this can use a BuildService
    @Internal
    public AbstractDecompiler getAbstractDecompiler() {
        return this.getDecompiler().get().getProvider().provide(this.getProject());
    }

    public void classJavadocProvider(ClassJavadocProvider provider) {
        this.classJavadocProvider = provider;
    }

    public void fieldJavadocProvider(FieldJavadocProvider provider) {
        this.fieldJavadocProvider = provider;
    }

    public void methodJavadocProvider(MethodJavadocProvider provider) {
        this.methodJavadocProvider = provider;
    }
}
