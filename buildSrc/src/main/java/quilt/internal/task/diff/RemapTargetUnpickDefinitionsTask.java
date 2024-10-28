package quilt.internal.task.diff;

import org.gradle.api.tasks.TaskContainer;
import quilt.internal.plugin.TargetDiffPlugin;
import quilt.internal.task.unpick.RemapUnpickDefinitionsTask;

/**
 * @see TargetDiffPlugin TargetDiffPlugin's configureEach[es]
 */
public abstract class RemapTargetUnpickDefinitionsTask extends RemapUnpickDefinitionsTask implements
        UnpickVersionsMatchConsumingTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link TargetDiffPlugin}.
     */
    public static final String REMAP_TARGET_UNPICK_DEFINITIONS_TASK_NAME = "remapTargetUnpickDefinitions";
}
