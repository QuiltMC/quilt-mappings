package quilt.internal.tasks.build;

import quilt.internal.Constants;
import quilt.internal.tasks.setup.DownloadPerVersionMappingsTask;

public class InvertPerVersionMappingsTask extends AbstractInvertMappingsTask {
    public static final String TASK_NAME = "invertPerVersionMappings";

    public InvertPerVersionMappingsTask() {
        super(Constants.PER_VERSION_MAPPINGS_NAME);
        this.dependsOn(DownloadPerVersionMappingsTask.TASK_NAME);

        input.convention(getTaskByType(DownloadPerVersionMappingsTask.class)::getTinyFile);
    }
}
