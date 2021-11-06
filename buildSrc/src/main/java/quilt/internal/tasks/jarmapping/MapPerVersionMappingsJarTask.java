package quilt.internal.tasks.jarmapping;

import java.io.File;

import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.setup.DownloadMinecraftLibrariesTask;
import quilt.internal.tasks.setup.DownloadPerVersionMappingsTask;
import quilt.internal.tasks.setup.MergeJarsTask;
import quilt.internal.util.JarRemapper;

public class MapPerVersionMappingsJarTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "mapHashedMojmapJar";

    public MapPerVersionMappingsJarTask() {
        super(Constants.Groups.MAP_JAR_GROUP);

        getInputs().files(getTaskByName(DownloadMinecraftLibrariesTask.TASK_NAME).getOutputs().getFiles().getFiles());
        getOutputs().file(this.fileConstants.hashedMojmapJar);

        this.dependsOn(DownloadMinecraftLibrariesTask.TASK_NAME, DownloadPerVersionMappingsTask.TASK_NAME, MergeJarsTask.TASK_NAME);

        outputsNeverUpToDate();
    }

    @TaskAction
    public void mapPerVersionMappingJar() {
        getLogger().lifecycle(":mapping minecraft to {}", Constants.PER_VERSION_MAPPINGS_NAME);
        File tinyInput = getTaskByType(DownloadPerVersionMappingsTask.class).getTinyFile();
        JarRemapper.mapJar(fileConstants.hashedMojmapJar, getTaskByType(MergeJarsTask.class).getMergedFile(), tinyInput, fileConstants.libraries, "official", Constants.PER_VERSION_MAPPINGS_NAME, null);
    }
}
