package quilt.internal.tasks.jarmapping;

import java.util.Map;

import quilt.internal.Constants;
import quilt.internal.MappingsPlugin;
import quilt.internal.tasks.build.AddProposedMappingsTask;
import quilt.internal.tasks.setup.DownloadMinecraftLibrariesTask;

public class MapNamedJarTask extends MapJarTask {
    public static final String TASK_NAME = "mapNamedJar";

    public MapNamedJarTask() {
        super(Constants.Groups.MAP_JAR_GROUP, Constants.PER_VERSION_MAPPINGS_NAME, "named");

        getInputs().files(getTaskByName(DownloadMinecraftLibrariesTask.TASK_NAME).getOutputs().getFiles().getFiles());

        inputJar.convention(() -> fileConstants.unpickedJar);
        mappingsFile.convention(() -> this.<AddProposedMappingsTask>getTaskByName(MappingsPlugin.INSERT_AUTO_GENERATED_MAPPINGS_TASK_NAME).getOutputMappings());
        outputJar.convention(() -> fileConstants.namedJar);

        this.dependsOn(MappingsPlugin.INSERT_AUTO_GENERATED_MAPPINGS_TASK_NAME, "unpickHashedJar");
    }

    public Map<String, String> getAdditionalMappings() {
        return JAVAX_TO_JETBRAINS;
    }
}

