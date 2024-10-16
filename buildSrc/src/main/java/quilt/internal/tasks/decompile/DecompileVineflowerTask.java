package quilt.internal.tasks.decompile;

import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.decompile.Decompilers;

import java.io.IOException;

public abstract class DecompileVineflowerTask extends DecompileTask {
    public static final String TASK_NAME = "decompileVineflower";

    public DecompileVineflowerTask() {
        this.getDecompiler().set(Decompilers.VINEFLOWER);
        this.getDecompiler().finalizeValue();
    }

    @Override
    @TaskAction
    public void decompile() throws IOException {
        FileUtils.deleteDirectory(this.getOutput().get().getAsFile());

        super.decompile();
    }
}
