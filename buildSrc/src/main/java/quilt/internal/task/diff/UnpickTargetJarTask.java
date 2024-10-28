package quilt.internal.task.diff;

import org.gradle.api.tasks.TaskContainer;
import quilt.internal.plugin.TargetDiffPlugin;
import quilt.internal.task.unpick.UnpickJarTask;

/**
 * @see TargetDiffPlugin TargetDiffPlugin's configureEach[es]
 */
public abstract class UnpickTargetJarTask extends UnpickJarTask implements UnpickVersionsMatchConsumingTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link TargetDiffPlugin}.
     */
    public static final String UNPICK_TARGET_JAR_TASK_NAME = "unpickTargetJar";
}
