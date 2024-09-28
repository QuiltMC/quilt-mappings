package quilt.internal.tasks.diff;

import java.util.Map;

import quilt.internal.Constants;
import quilt.internal.tasks.jarmapping.MapJarTask;

public abstract class RemapTargetMinecraftJarTask extends MapJarTask {
    public static final String TASK_NAME = "remapTargetMinecraftJar";

    public RemapTargetMinecraftJarTask() {
        super("diff", Constants.PER_VERSION_MAPPINGS_NAME, "named");

        final CheckTargetVersionExistsTask checkExists =
            this.getTaskNamed(CheckTargetVersionExistsTask.TASK_NAME, CheckTargetVersionExistsTask.class);
        final CheckUnpickVersionsMatchTask checkUnpickExists =
            this.getTaskNamed(CheckUnpickVersionsMatchTask.TASK_NAME, CheckUnpickVersionsMatchTask.class);

        this.onlyIf(task -> checkExists.getTargetVersion().isPresent() && checkUnpickExists.isMatch());
        this.dependsOn(DownloadTargetMappingJarTask.TASK_NAME, "unpickTargetJar");
        final DownloadTargetMappingJarTask downloadTarget =
            this.getTaskNamed(DownloadTargetMappingJarTask.TASK_NAME, DownloadTargetMappingJarTask.class);

        this.getInputJar().convention(() -> this.getProject().file(
            DownloadTargetMappingJarTask.TARGET_MAPPINGS + "/quilt-mappings-" +
                checkExists.getTargetVersion().orElse(Constants.MAPPINGS_VERSION) + "-unpicked.jar"
        ));
        this.getMappingsFile().set(downloadTarget.getTargetMappingsFile());
        this.getOutputJar().convention(() -> this.getProject().file(
            DownloadTargetMappingJarTask.TARGET_MAPPINGS + "/quilt-mappings-" +
                checkExists.getTargetVersion().orElse(Constants.MAPPINGS_VERSION) + "-named.jar"
        ));
    }

    public Map<String, String> getAdditionalMappings() {
        return MapJarTask.JAVAX_TO_JETBRAINS;
    }
}
