package quilt.internal.tasks.diff;

import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import quilt.internal.tasks.decompile.DecompileTask;
import quilt.internal.decompile.javadoc.MappingsJavadocProvider;

import java.io.IOException;

public abstract class DecompileTargetTask extends DecompileTask implements UnpickVersionsMatchConsumingTask {
    // @Input
    // public abstract Property<String> getJavadocNamespace();
    //
    // @InputFile
    // public abstract RegularFileProperty getJavadocSource();

    @Override
    public void decompile() throws IOException {
        FileUtils.deleteDirectory(this.getOutput().get().getAsFile());

        // try {
        //     final MappingsJavadocProvider javadocProvider = new MappingsJavadocProvider(
        //         this.getJavadocSource().get().getAsFile(), this.getJavadocNamespace().get()
        //     );
        //
        //     this.classJavadocProvider(javadocProvider);
        //     this.fieldJavadocProvider(javadocProvider);
        //     this.methodJavadocProvider(javadocProvider);
        // } catch (IOException e) {
        //     throw new GradleException("Failed to create javadoc provider", e);
        // }

        super.decompile();
    }
}
