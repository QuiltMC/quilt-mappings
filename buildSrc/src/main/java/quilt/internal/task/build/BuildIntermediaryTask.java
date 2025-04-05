package quilt.internal.task.build;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskContainer;
import quilt.internal.constants.Groups;
import quilt.internal.plugin.MapIntermediaryPlugin;

public abstract class BuildIntermediaryTask extends DefaultTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapIntermediaryPlugin}.
     */
    public static final String BUILD_INTERMEDIARY_TASK_NAME = "buildIntermediary";

    public BuildIntermediaryTask() {
        this.setGroup(Groups.BUILD_MAPPINGS);
    }
}
