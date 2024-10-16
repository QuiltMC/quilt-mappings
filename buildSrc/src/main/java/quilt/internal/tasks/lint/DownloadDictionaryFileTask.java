package quilt.internal.tasks.lint;

import java.io.IOException;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.DownloadTask;

public abstract class DownloadDictionaryFileTask extends DefaultMappingsTask implements DownloadTask {
    public static final String DOWNLOAD_DICTIONARY_FILE_TASK_NAME = "downloadDictionaryFile";

    @Input
    public abstract Property<String> getUrl();

    @OutputFile
    public abstract RegularFileProperty getOutput();

    public DownloadDictionaryFileTask() {
        super(Constants.Groups.LINT);
    }

    @TaskAction
    public void downloadDictionaryFile() throws IOException {
        this.startDownload()
            .src(this.getUrl().get())
            .overwrite(false)
            .dest(this.getOutput().get().getAsFile())
            .download();
    }
}
