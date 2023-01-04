package quilt.internal.tasks.diff;

import java.util.Map;

import quilt.internal.Constants;
import quilt.internal.tasks.jarmapping.MapJarTask;

public class RemapTargetMinecraftJarTask extends MapJarTask {
    public static final String TASK_NAME = "remapTargetMinecraftJar";

    public RemapTargetMinecraftJarTask() {
        super("diff", Constants.PER_VERSION_MAPPINGS_NAME, "named");

        CheckTargetVersionExistsTask checkExists = getTaskByType(CheckTargetVersionExistsTask.class);
        this.onlyIf(task -> checkExists.getTargetVersion().isPresent());
        this.dependsOn(DownloadTargetMappingJarTask.TASK_NAME);
        DownloadTargetMappingJarTask downloadTarget = getTaskByType(DownloadTargetMappingJarTask.class);

        inputJar.convention(() -> fileConstants.unpickedJar);
        mappingsFile.set(downloadTarget.getTargetMappingsFile());
        outputJar.convention(() -> getProject().file(DownloadTargetMappingJarTask.TARGET_MAPPINGS + "/quilt-mappings-" + checkExists.getTargetVersion().orElse(Constants.MAPPINGS_VERSION) + "-named.jar"));
    }

    public Map<String, String> getAdditionalMappings() {
        return Map.of(
                "javax/annotation/Nullable", "org/jetbrains/annotations/Nullable",
                "javax/annotation/Nonnull", "org/jetbrains/annotations/NotNull",
                "javax/annotation/concurrent/Immutable", "org/jetbrains/annotations/Unmodifiable"
        );
    }
}
