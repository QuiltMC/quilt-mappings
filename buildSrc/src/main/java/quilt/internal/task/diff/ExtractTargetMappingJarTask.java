package quilt.internal.task.diff;

import org.gradle.api.tasks.TaskContainer;
import quilt.internal.constants.Groups;
import quilt.internal.plugin.TargetDiffPlugin;
import quilt.internal.task.ExtractZippedFilesTask;

/**
 * @see TargetDiffPlugin TargetDiffPlugin's configureEach
 */
public abstract class ExtractTargetMappingJarTask extends ExtractZippedFilesTask implements TargetVersionConsumingTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link TargetDiffPlugin}.
     */
    public static final String EXTRACT_TARGET_MAPPINGS_JAR_TASK_NAME = "extractTargetMappingsJar";

    public ExtractTargetMappingJarTask() {
        super(Groups.DIFF);
    }
}
