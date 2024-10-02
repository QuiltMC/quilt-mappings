package quilt.internal.tasks.diff;

import quilt.internal.tasks.unpick.RemapUnpickDefinitionsTask;

public abstract class RemapTargetUnpickDefinitionsTask extends RemapUnpickDefinitionsTask implements
        UnpickVersionsMatchConsumingTask {
    public static final String TASK_NAME = "remapTargetUnpickDefinitions";
}
