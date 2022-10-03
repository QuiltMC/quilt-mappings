package quilt.internal.tasks.setup;

import quilt.internal.Constants;

public class DownloadPerVersionMappingsTask extends AbstractDownloadMappingsTask {
    public static final String TASK_NAME = "downloadPerVersionMappings";

    public DownloadPerVersionMappingsTask() {
        super(Constants.PER_VERSION_MAPPINGS_NAME);
    }
}
