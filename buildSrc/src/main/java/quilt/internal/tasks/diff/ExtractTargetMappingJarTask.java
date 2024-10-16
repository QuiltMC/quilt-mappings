package quilt.internal.tasks.diff;

import quilt.internal.Constants;
import quilt.internal.tasks.ExtractZippedFilesTask;

public abstract class ExtractTargetMappingJarTask extends ExtractZippedFilesTask implements TargetVersionConsumingTask {
    public static final String TASK_NAME = "extractTargetMappingsJar";

    public ExtractTargetMappingJarTask() {
        super(Constants.Groups.DIFF);
    }
}
