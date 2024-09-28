package quilt.internal.tasks.jarmapping;

import quilt.internal.Constants;
import quilt.internal.tasks.setup.DownloadMinecraftLibrariesTask;
import quilt.internal.tasks.setup.DownloadPerVersionMappingsTask;
import quilt.internal.tasks.setup.MergeJarsTask;

public abstract class MapPerVersionMappingsJarTask extends MapJarTask {
    public static final String TASK_NAME = "mapPerVersionMappingsJar";

    public MapPerVersionMappingsJarTask() {
        super(Constants.Groups.MAP_JAR_GROUP, "official", Constants.PER_VERSION_MAPPINGS_NAME);
        this.dependsOn(
            DownloadMinecraftLibrariesTask.TASK_NAME,
            DownloadPerVersionMappingsTask.TASK_NAME,
            MergeJarsTask.TASK_NAME
        );

        this.getInputJar().convention(this.getTaskNamed(MergeJarsTask.TASK_NAME, MergeJarsTask.class).getMergedFile());
        this.getMappingsFile().convention(
            this.getTaskNamed(DownloadPerVersionMappingsTask.TASK_NAME, DownloadPerVersionMappingsTask.class)
                .getTinyFile()
        );
        this.getOutputJar().convention(() -> this.fileConstants.perVersionMappingsJar);

        this.getInputs().files(this.fileConstants.libraries);
    }
}
