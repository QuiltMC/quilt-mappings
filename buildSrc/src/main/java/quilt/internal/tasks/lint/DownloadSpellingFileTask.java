package quilt.internal.tasks.lint;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public class DownloadSpellingFileTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadSpellingFile";

    public static final String DEFAULT_SPELLING_FILE = "https://github.com/dwyl/english-words/blob/a77cb15f4f5beb59c15b945f2415328a6b33c3b0/words_alpha.txt?raw=true";
    @OutputFile
    private final File output;

    @Input
    public final Property<String> url;

    public DownloadSpellingFileTask() {
        super(Constants.Groups.LINT_GROUP);

        output = this.mappingsExt().getFileConstants().spellingFile;

        url = getProject().getObjects().property(String.class);
        url.convention(DEFAULT_SPELLING_FILE);
    }

    @TaskAction
    public void downloadSpellingFile() throws IOException {
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
