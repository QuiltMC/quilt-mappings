package quilt.internal.tasks.lint;

import quilt.internal.Constants.Groups;
import quilt.internal.tasks.SimpleDownloadTask;

public abstract class DownloadDictionaryFileTask extends SimpleDownloadTask {
    public static final String DOWNLOAD_DICTIONARY_FILE_TASK_NAME = "downloadDictionaryFile";

    public DownloadDictionaryFileTask() {
        super(Groups.LINT);
    }
}
