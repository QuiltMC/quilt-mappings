package quilt.internal.tasks.jarmapping;

import java.io.File;

import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.build.DownloadPerVersionMappingsTask;
import quilt.internal.util.JarRemapper;

public class MapPerVersionMappingsJarTask extends DefaultMappingsTask {
    public MapPerVersionMappingsJarTask() {
        super(Constants.Groups.MAP_JAR_GROUP);

        getInputs().files(getTaskFromName("downloadMinecraftLibraries").getOutputs().getFiles().getFiles());
        getOutputs().file(this.fileConstants.hashedMojmapJar);

        this.dependsOn("downloadMinecraftLibraries", "downloadHashedMojmap", "mergeJars");

        outputsNeverUpToDate();
    }

    @TaskAction
    public void mapPerVersionMappingJar() {
        getLogger().lifecycle(":mapping minecraft to " + Constants.PER_VERSION_MAPPINGS_NAME);
        File tinyInput = getTaskFromType(DownloadPerVersionMappingsTask.class).getTinyFile();
        JarRemapper.mapJar(fileConstants.hashedMojmapJar, fileConstants.mergedFile, tinyInput, fileConstants.libraries, "official", Constants.PER_VERSION_MAPPINGS_NAME, null);
    }
}
