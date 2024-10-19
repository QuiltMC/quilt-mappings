package quilt.internal.tasks;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.util.DownloadUtil;

import static quilt.internal.util.ProviderUtil.toOptional;

public abstract class SimpleDownloadTask extends DefaultMappingsTask {
    @Input
    public abstract Property<String> getUrl();

    /**
     * Whether to overwrite {@link #getDest() dest} if it already {@link java.io.File#exists() exists}.
     * <p>
     * This will be {@code true} if no specified.
     */
    @Optional
    @Input
    public abstract Property<Boolean> getOverwrite();

    /**
     * If {@link org.gradle.api.provider.Provider#isPresent() present}, the held message will be
     * {@linkplain org.gradle.api.logging.Logger#lifecycle(String) logged}
     * before the {@link #download} starts.
     */
    @Optional
    @Input
    public abstract Property<String> getPreDownloadLifecycle();

    /**
     * If {@link org.gradle.api.provider.Provider#isPresent() present}, the held message will be
     * {@linkplain org.gradle.api.logging.Logger#lifecycle(String) logged}
     * after the {@link #download} completes.
     */
    @Optional
    @Input
    public abstract Property<String> getPostDownloadLifecycle();

    @OutputFile
    public abstract RegularFileProperty getDest();

    public SimpleDownloadTask(String group) {
        super(group);
    }

    @TaskAction
    public void download() {
        toOptional(this.getPreDownloadLifecycle()).ifPresent(this.getLogger()::lifecycle);

        DownloadUtil.download(
            this.getUrl().get(),
            this.getDest().get().getAsFile(),
            this.getOverwrite().getOrElse(true),
            this.getLogger()
        );

        toOptional(this.getPostDownloadLifecycle()).ifPresent(this.getLogger()::lifecycle);
    }
}
