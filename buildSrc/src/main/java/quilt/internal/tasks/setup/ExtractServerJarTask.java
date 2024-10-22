package quilt.internal.tasks.setup;

import quilt.internal.Constants.Groups;
import quilt.internal.tasks.ExtractSingleZippedFileTask;

public abstract class ExtractServerJarTask extends ExtractSingleZippedFileTask {
    public static final String EXTRACT_SERVER_JAR_TASK_NAME = "extractServerJar";

    public static final String SERVER_JAR_PATTERN = "META-INF/versions/*/server-*.jar";

    public ExtractServerJarTask() {
        super(
            Groups.SETUP,
            filterable -> filterable.include(SERVER_JAR_PATTERN)
        );
    }
}
