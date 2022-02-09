package quilt.internal.tasks.jarmapping;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.setup.DownloadMinecraftLibrariesTask;
import quilt.internal.tasks.setup.DownloadPerVersionMappingsTask;
import quilt.internal.tasks.setup.MergeJarsTask;
import quilt.internal.util.JarRemapper;

public class MapPerVersionMappingsJarTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "mapPerVersionMappingsJar";

    @InputFile
    private final RegularFileProperty mergedJar;

    @InputFile
    private final RegularFileProperty perVersionMappingsFile;

    public MapPerVersionMappingsJarTask() {
        super(Constants.Groups.MAP_JAR_GROUP);
        this.dependsOn(DownloadMinecraftLibrariesTask.TASK_NAME, DownloadPerVersionMappingsTask.TASK_NAME, MergeJarsTask.TASK_NAME);

        mergedJar = getProject().getObjects().fileProperty();
        perVersionMappingsFile = getProject().getObjects().fileProperty();

        mergedJar.convention(getTaskByType(MergeJarsTask.class)::getMergedFile);
        perVersionMappingsFile.convention(getTaskByType(DownloadPerVersionMappingsTask.class)::getTinyFile);

        getInputs().files(fileConstants.libraries);
        getOutputs().file(this.fileConstants.perVersionMappingsJar);
    }

    @TaskAction
    public void mapPerVersionMappingJar() {
        getLogger().lifecycle(":mapping minecraft to {}", Constants.PER_VERSION_MAPPINGS_NAME);
        JarRemapper.mapJar(fileConstants.perVersionMappingsJar, mergedJar.get().getAsFile(), perVersionMappingsFile.get().getAsFile(), fileConstants.libraries, "official", Constants.PER_VERSION_MAPPINGS_NAME, null);
    }

    public RegularFileProperty getMergedJar() {
        return mergedJar;
    }

    public RegularFileProperty getPerVersionMappingsFile() {
        return perVersionMappingsFile;
    }
}
