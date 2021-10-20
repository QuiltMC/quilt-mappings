package quilt.internal.tasks.jarmapping;

import java.io.File;
import java.util.Map;

import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.build.DownloadPerVersionMappingsTask;
import quilt.internal.tasks.setup.DownloadMinecraftLibrariesTask;
import quilt.internal.tasks.setup.MergeJarsTask;
import quilt.internal.util.JarRemapper;

public class MapNamedJarTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "mapNamedJar";

    public MapNamedJarTask() {
        super(Constants.Groups.MAP_JAR_GROUP);

        getInputs().files(getTaskByName(DownloadMinecraftLibrariesTask.TASK_NAME).getOutputs().getFiles().getFiles());
        getOutputs().file(this.fileConstants.namedJar);

        this.dependsOn("mergeV2", "unpickHashedJar");

        outputsNeverUpToDate();
    }

    @TaskAction
    public void mapPerVersionMappingJar() {
        getLogger().lifecycle(":mapping minecraft to named");
        File tinyInput = getTaskByType(DownloadPerVersionMappingsTask.class).getTinyFile();
        Map<String, String> jsrToJetbrains = Map.of(
                "javax/annotation/Nullable", "org/jetbrains/annotations/Nullable",
                "javax/annotation/Nonnull", "org/jetbrains/annotations/NotNull",
                "javax/annotation/concurrent/Immutable", "org/jetbrains/annotations/Unmodifiable"
        );
        JarRemapper.mapJar(fileConstants.hashedMojmapJar, getTaskByType(MergeJarsTask.class).getMergedFile(), tinyInput, fileConstants.libraries, "official", Constants.PER_VERSION_MAPPINGS_NAME, builder -> builder.withMappings(out -> jsrToJetbrains.forEach(out::acceptClass)));
    }
}

