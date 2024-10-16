package quilt.internal.tasks.diff;

import java.io.File;
import java.io.IOException;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.DownloadTask;

public abstract class DownloadTargetMappingJarTask extends DefaultMappingsTask implements
        TargetVersionConsumingTask, DownloadTask {
    public static final String DOWNLOAD_TARGET_MAPPINGS_JAR_TASK_NAME = "downloadTargetMappingsJar";

    @OutputFile
    public abstract RegularFileProperty getTargetJar();

    @OutputFile
    public abstract RegularFileProperty getTargetUnpickConstantsFile();

    public DownloadTargetMappingJarTask() {
        super(Constants.Groups.DIFF);
    }

    @TaskAction
    public void downloadTargetMappings() throws IOException {
        final String targetVersion = this.getTargetVersion().get();

        final File targetMappingsJar = this.getTargetJar().get().getAsFile();

        final String urlPrefix = "https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-mappings/" +
            targetVersion + "/quilt-mappings-" + targetVersion;

        this.startDownload()
                .src(urlPrefix + "-v2.jar")
                .dest(targetMappingsJar)
                .download();

        this.startDownload()
                .src(urlPrefix + "-constants.jar")
                .dest(this.getTargetUnpickConstantsFile().get().getAsFile())
                .download();
    }
}
