package quilt.internal.task.diff;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskContainer;
import quilt.internal.constants.Classifiers;
import quilt.internal.constants.Extensions;
import quilt.internal.constants.Groups;
import quilt.internal.plugin.TargetDiffPlugin;
import quilt.internal.task.DefaultMappingsTask;
import quilt.internal.util.DownloadUtil;

/**
 * @see TargetDiffPlugin TargetDiffPlugin's configureEach
 */
public abstract class DownloadTargetMappingJarTask extends DefaultMappingsTask implements TargetVersionConsumingTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link TargetDiffPlugin}.
     */
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
            urlPrefix + "-" + Classifiers.V2 + "." + Extensions.JAR,
            this.getTargetJar().get().getAsFile(),
            false, this.getLogger()
        );

        DownloadUtil.download(
            urlPrefix + "-" + Classifiers.CONSTANTS + "." + Extensions.JAR,
            this.getTargetUnpickConstantsFile().get().getAsFile(),
            false, this.getLogger()
        );
    }
}
