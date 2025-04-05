package quilt.internal.task.setup;

import org.gradle.api.tasks.TaskContainer;
import quilt.internal.constants.Extensions;
import quilt.internal.constants.Groups;
import quilt.internal.plugin.MapMinecraftJarsPlugin;
import quilt.internal.task.ExtractSingleZippedFileTask;

public abstract class ExtractTinyMappingsTask extends ExtractSingleZippedFileTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapMinecraftJarsPlugin}.
     */
    public static final String EXTRACT_TINY_INTERMEDIATE_MAPPINGS_TASK_NAME = "extractTinyIntermediateMappings";

    private static final String TINY_MAPPINGS_PATTERN = "**/*mappings." + Extensions.TINY;

    public ExtractTinyMappingsTask() {
        super(filterable -> filterable.include(TINY_MAPPINGS_PATTERN));

        this.setGroup(Groups.SETUP);
    }
}
