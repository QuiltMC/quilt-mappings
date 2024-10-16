package quilt.internal.tasks.diff;

import quilt.internal.tasks.decompile.DecompileVineflowerTask;

public abstract class DecompileTargetVineflowerTask extends DecompileVineflowerTask implements
        TargetVersionConsumingTask {
    public static final String TASK_NAME = "decompileTargetVineflower";
}
