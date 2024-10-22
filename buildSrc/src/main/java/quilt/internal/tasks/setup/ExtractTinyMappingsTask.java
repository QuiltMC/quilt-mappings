package quilt.internal.tasks.setup;

import quilt.internal.Constants;
import quilt.internal.tasks.ExtractSingleZippedFileTask;

public abstract class ExtractTinyMappingsTask extends ExtractSingleZippedFileTask {
    public static final String EXTRACT_TINY_PER_VERSION_MAPPINGS_TASK_NAME = "extractTinyPerVersionMappings";

    private static final String TINY_MAPPINGS_PATTERN = "**/*mappings.tiny";

    public ExtractTinyMappingsTask() {
        super(
            Constants.Groups.SETUP,
            filterable -> filterable.include(TINY_MAPPINGS_PATTERN)
        );
    }
}
