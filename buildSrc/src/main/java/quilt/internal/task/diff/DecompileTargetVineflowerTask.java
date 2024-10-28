package quilt.internal.task.diff;

import org.gradle.api.tasks.TaskContainer;
import quilt.internal.plugin.TargetDiffPlugin;
import quilt.internal.task.decompile.DecompileVineflowerTask;

/**
 * @see TargetDiffPlugin TargetDiffPlugin's configureEach
 */
public abstract class DecompileTargetVineflowerTask extends DecompileVineflowerTask implements
        TargetVersionConsumingTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link TargetDiffPlugin}.
     */
    public static final String DECOMPILE_TARGET_VINEFLOWER_TASK_NAME = "decompileTargetVineflower";
}
