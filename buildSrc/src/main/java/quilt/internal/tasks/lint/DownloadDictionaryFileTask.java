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
    // TODO this should probably be published to quilt maven, see #442
    public static final String DEFAULT_DICTIONARY_FILE =
        "https://raw.githubusercontent.com/ix0rai/qm-base-allowed-wordlist/" + REVISION + "/allowed_english_words.txt";

    // TODO make this configurable via MappingsExtension, put the default value in build.gradle and/or libs.version.toml
    @Input
    public abstract Property<String> getUrl();

    @OutputFile
    public abstract RegularFileProperty getOutput();

    public DownloadDictionaryFileTask() {
        super(Constants.Groups.LINT_GROUP);

        this.getUrl().convention(DEFAULT_DICTIONARY_FILE);

        this.getOutput().convention(() -> this.mappingsExt().getFileConstants().dictionaryFile);
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
