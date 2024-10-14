package quilt.internal.tasks.decompile;

import org.apache.commons.io.FileUtils;
import quilt.internal.decompile.Decompilers;
import quilt.internal.decompile.javadoc.MappingsJavadocProvider;

import java.io.IOException;

// TODO rename this and its name to "decompile with vineflower"
//  once generateDiff task eliminates magic strings
public abstract class DecompileVineflowerTask extends DecompileTask {
    public DecompileVineflowerTask() {
        this.getDecompiler().set(Decompilers.VINEFLOWER);
        this.getDecompiler().finalizeValue();
    }

    @Override
    public void decompile() throws IOException {
        FileUtils.deleteDirectory(this.getOutput().get().getAsFile());

        // def javadocProvider =
        //     new MappingsJavadocProvider(insertAutoGeneratedMappings.outputMappings.get().getAsFile(), "named");
        // classJavadocProvider(javadocProvider);
        // fieldJavadocProvider(javadocProvider);
        // methodJavadocProvider(javadocProvider);

        super.decompile();
    }
}
