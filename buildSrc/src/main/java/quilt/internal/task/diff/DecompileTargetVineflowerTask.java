package quilt.internal.task.diff;

import quilt.internal.task.decompile.DecompileVineflowerTask;

public abstract class DecompileTargetVineflowerTask extends DecompileVineflowerTask implements
        TargetVersionConsumingTask {
    public static final String DECOMPILE_TARGET_VINEFLOWER_TASK_NAME = "decompileTargetVineflower";
}
