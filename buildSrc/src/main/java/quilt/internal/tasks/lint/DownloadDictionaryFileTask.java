package quilt.internal.tasks.lint;

import java.io.IOException;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public abstract class DownloadDictionaryFileTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadDictionaryFile";

    public static final String REVISION = "f9c2abb8ad2df8bf64df06ae2f6ede86704b82c7";
    public static final String DEFAULT_DICTIONARY_FILE =
        "https://raw.githubusercontent.com/ix0rai/qm-base-allowed-wordlist/" + REVISION + "/allowed_english_words.txt";

    @OutputFile
    public abstract RegularFileProperty getOutput();

    @Input
    public abstract Property<String> getUrl();

    public DownloadDictionaryFileTask() {
        super(Constants.Groups.LINT_GROUP);

        this.getOutput().convention(() -> this.mappingsExt().getFileConstants().dictionaryFile);

        this.getUrl().convention(DEFAULT_DICTIONARY_FILE);
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
