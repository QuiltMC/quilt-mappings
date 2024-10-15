package quilt.internal.tasks.diff;

import org.apache.commons.io.FileUtils;
import quilt.internal.tasks.decompile.DecompileTask;

import java.io.IOException;

public abstract class DecompileTargetTask extends DecompileTask implements UnpickVersionsMatchConsumingTask {
    @Override
    public void decompile() throws IOException {
        FileUtils.deleteDirectory(this.getOutput().get().getAsFile());

        super.decompile();
    }
}
