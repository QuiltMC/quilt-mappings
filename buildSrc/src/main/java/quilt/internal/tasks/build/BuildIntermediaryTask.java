package quilt.internal.tasks.build;

import org.gradle.api.tasks.TaskContainer;
import quilt.internal.Constants.Groups;
import quilt.internal.plugin.MapIntermediaryPlugin;
import quilt.internal.tasks.DefaultMappingsTask;

public abstract class BuildIntermediaryTask extends DefaultMappingsTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapIntermediaryPlugin}.
     */
    public static final String BUILD_INTERMEDIARY_TASK_NAME = "buildIntermediary";

    public BuildIntermediaryTask() {
        super(Groups.BUILD_MAPPINGS);
    }
}
