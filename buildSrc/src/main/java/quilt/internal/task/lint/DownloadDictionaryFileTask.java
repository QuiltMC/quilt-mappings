package quilt.internal.task.lint;

import quilt.internal.Constants.Groups;
import quilt.internal.plugin.MappingsVerificationPlugin;
import quilt.internal.task.SimpleDownloadTask;

public abstract class DownloadDictionaryFileTask extends SimpleDownloadTask {
    /**
     * {@linkplain org.gradle.api.tasks.TaskContainer#register Registered} by {@link MappingsVerificationPlugin}.
     */
    public static final String DOWNLOAD_DICTIONARY_FILE_TASK_NAME = "downloadDictionaryFile";

    public DownloadDictionaryFileTask() {
        super(Groups.LINT);
    }
}
