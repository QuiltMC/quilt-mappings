package quilt.internal.tasks.build;

import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import quilt.internal.tasks.decompile.DecompileTask;
import quilt.internal.decompile.javadoc.MappingsJavadocProvider;

import java.io.IOException;
import java.util.Map;

public abstract class GenFakeSourceTask extends DecompileTask {
    public static final String TASK_NAME = "genFakeSource";

    public GenFakeSourceTask() {
        this.getDecompilerOptions().putAll(Map.of(
            // remove synthetics
            "rsy", "1",
            // decompile generic signatures
            "dgs", "1",
            // pll (Preferred Line Length) is length for line wrapping
            "pll", "99999"
        ));
    }

    @Override
    public void decompile() throws IOException {
        FileUtils.deleteDirectory(this.getOutput().get().getAsFile());

        super.decompile();

        this.getLogger().lifecycle(":Fake source generated");
    }
}
