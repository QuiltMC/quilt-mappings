package quilt.internal.tasks.lint;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public class DownloadDictionaryFileTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadDictionaryFile";

    public static final String DEFAULT_DICTIONARY_FILE = "https://github.com/ix0rai/qm-base-allowed-wordlist/2ab38ea58ccbd9b998eaa537cf07650d02ecb659/allowed_english_words.txt?raw=true";
    @OutputFile
    private final File output;

    @Input
    public final Property<String> url;

    public DownloadDictionaryFileTask() {
        super(Constants.Groups.LINT_GROUP);

        output = this.mappingsExt().getFileConstants().dictionaryFile;

        url = getProject().getObjects().property(String.class);
        url.convention(DEFAULT_DICTIONARY_FILE);
    }

    @TaskAction
    public void downloadDictionaryFile() throws IOException {
        this.startDownload()
                .src(url.get())
                .overwrite(false)
                .dest(output)
                .download();
    }

    public Property<String> getUrl() {
        return url;
    }

    public File getOutput() {
        return output;
    }
}
