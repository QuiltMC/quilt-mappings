package quilt.internal.tasks.build;

import quilt.internal.Constants.Groups;
import quilt.internal.tasks.DefaultMappingsTask;

public abstract class BuildIntermediaryTask extends DefaultMappingsTask {
    public static final String BUILD_INTERMEDIARY_TASK_NAME = "buildIntermediary";

    public BuildIntermediaryTask() {
        super(Groups.BUILD_MAPPINGS);
    }
}
