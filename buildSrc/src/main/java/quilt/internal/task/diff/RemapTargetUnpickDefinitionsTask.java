package quilt.internal.task.diff;

import quilt.internal.task.unpick.RemapUnpickDefinitionsTask;

public abstract class RemapTargetUnpickDefinitionsTask extends RemapUnpickDefinitionsTask implements
        UnpickVersionsMatchConsumingTask {
    public static final String REMAP_TARGET_UNPICK_DEFINITIONS_TASK_NAME = "remapTargetUnpickDefinitions";
}
