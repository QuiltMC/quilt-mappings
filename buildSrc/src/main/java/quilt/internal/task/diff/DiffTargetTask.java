package quilt.internal.task.diff;

import org.gradle.api.tasks.TaskContainer;
import quilt.internal.plugin.TargetDiffPlugin;

public abstract class DiffTargetTask extends DiffDirectoriesTask implements TargetVersionConsumingTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link TargetDiffPlugin}.
     */
    public static final String DIFF_TARGET_TASK_NAME = "diffTarget";
}
