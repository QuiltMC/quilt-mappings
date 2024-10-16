package quilt.internal.tasks.build;

import org.gradle.api.tasks.TaskAction;
import quilt.internal.tasks.decompile.DecompileVineflowerTask;

import java.io.IOException;
import java.util.Map;

public abstract class GenFakeSourceTask extends DecompileVineflowerTask {
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
    @TaskAction
    public void decompile() throws IOException {
        super.decompile();

        this.getLogger().lifecycle(":Fake source generated");
    }
}
