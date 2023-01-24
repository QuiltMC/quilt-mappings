package quilt.internal.tasks.diff;

import java.util.Map;

import quilt.internal.Constants;
import quilt.internal.tasks.jarmapping.MapJarTask;

public class RemapTargetMinecraftJarTask extends MapJarTask {
    public static final String TASK_NAME = "remapTargetMinecraftJar";

    public RemapTargetMinecraftJarTask() {
        super("diff", Constants.PER_VERSION_MAPPINGS_NAME, "named");

        CheckTargetVersionExistsTask checkExists = getTaskByType(CheckTargetVersionExistsTask.class);
        CheckUnpickVersionsMatchTask checkUnpickExists = getTaskByType(CheckUnpickVersionsMatchTask.class);

        this.onlyIf(task -> checkExists.getTargetVersion().isPresent() && checkUnpickExists.isMatch());
        this.dependsOn(DownloadTargetMappingJarTask.TASK_NAME, "unpickTargetJar");
        DownloadTargetMappingJarTask downloadTarget = getTaskByType(DownloadTargetMappingJarTask.class);

        inputJar.convention(() -> getProject().file(DownloadTargetMappingJarTask.TARGET_MAPPINGS + "/quilt-mappings-" + checkExists.getTargetVersion().orElse(Constants.MAPPINGS_VERSION) + "-unpicked.jar"));
        mappingsFile.set(downloadTarget.getTargetMappingsFile());
        outputJar.convention(() -> getProject().file(DownloadTargetMappingJarTask.TARGET_MAPPINGS + "/quilt-mappings-" + checkExists.getTargetVersion().orElse(Constants.MAPPINGS_VERSION) + "-named.jar"));
    }

    public Map<String, String> getAdditionalMappings() {
        return MapJarTask.JAVAX_TO_JETBRAINS;
    }
}
