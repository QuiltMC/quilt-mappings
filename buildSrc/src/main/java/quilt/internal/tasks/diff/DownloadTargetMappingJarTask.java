package quilt.internal.tasks.diff;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants.Groups;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.util.DownloadUtil;

public abstract class DownloadTargetMappingJarTask extends DefaultMappingsTask implements TargetVersionConsumingTask {
    public static final String DOWNLOAD_TARGET_MAPPINGS_JAR_TASK_NAME = "downloadTargetMappingsJar";

    @OutputFile
    public abstract RegularFileProperty getTargetJar();

    @OutputFile
    public abstract RegularFileProperty getTargetUnpickConstantsFile();

    public DownloadTargetMappingJarTask() {
        super(Groups.DIFF);
    }

    @TaskAction
    public void download() {
        final String targetVersion = this.getTargetVersion().get();

        final String urlPrefix = "https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-mappings/" +
            targetVersion + "/quilt-mappings-" + targetVersion;

        DownloadUtil.download(
            urlPrefix + "-v2.jar", this.getTargetJar().get().getAsFile(),
            false, this.getLogger()
        );

        DownloadUtil.download(
            urlPrefix + "-constants.jar", this.getTargetUnpickConstantsFile().get().getAsFile(),
            false, this.getLogger()
        );
    }
}
