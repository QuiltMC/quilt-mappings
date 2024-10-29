package quilt.internal.task.setup;

import quilt.internal.constants.Extensions;
import quilt.internal.constants.Groups;
import quilt.internal.plugin.MinecraftJarsPlugin;
import quilt.internal.task.ExtractSingleZippedFileTask;

public abstract class ExtractServerJarTask extends ExtractSingleZippedFileTask {
    /**
     * {@linkplain org.gradle.api.tasks.TaskContainer#register Registered} by
     * {@link MinecraftJarsPlugin MinecraftJarsPlugin}.
     */
    public static final String EXTRACT_SERVER_JAR_TASK_NAME = "extractServerJar";

    public static final String SERVER_JAR_PATTERN = "META-INF/versions/*/server-*." + Extensions.JAR;

    public ExtractServerJarTask() {
        super(
            Groups.SETUP,
            filterable -> filterable.include(SERVER_JAR_PATTERN)
        );
    }
}
