package quilt.internal.tasks.diff;

import quilt.internal.Constants.Groups;
import quilt.internal.tasks.ExtractZippedFilesTask;

public abstract class ExtractTargetMappingJarTask extends ExtractZippedFilesTask implements TargetVersionConsumingTask {
    public static final String EXTRACT_TARGET_MAPPINGS_JAR_TASK_NAME = "extractTargetMappingsJar";

    public ExtractTargetMappingJarTask() {
        super(Groups.DIFF);
    }
}
