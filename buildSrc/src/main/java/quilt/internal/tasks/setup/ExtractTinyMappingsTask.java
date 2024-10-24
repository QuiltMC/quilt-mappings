package quilt.internal.tasks.setup;

import org.gradle.api.tasks.TaskContainer;
import quilt.internal.Constants.Groups;
import quilt.internal.plugin.MapMinecraftJarsPlugin;
import quilt.internal.tasks.ExtractSingleZippedFileTask;

public abstract class ExtractTinyMappingsTask extends ExtractSingleZippedFileTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapMinecraftJarsPlugin}.
     */
    public static final String EXTRACT_TINY_PER_VERSION_MAPPINGS_TASK_NAME = "extractTinyPerVersionMappings";

    private static final String TINY_MAPPINGS_PATTERN = "**/*mappings.tiny";

    public ExtractTinyMappingsTask() {
        super(
            Groups.SETUP,
            filterable -> filterable.include(TINY_MAPPINGS_PATTERN)
        );
    }
}
