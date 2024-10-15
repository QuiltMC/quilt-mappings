package quilt.internal.tasks.diff;

import quilt.internal.Constants;
import quilt.internal.tasks.jarmapping.MapJarTask;

public abstract class RemapTargetMinecraftJarTask extends MapJarTask implements UnpickVersionsMatchConsumingTask {
    public static final String TASK_NAME = "remapTargetMinecraftJar";

    public RemapTargetMinecraftJarTask() {
        super("diff", Constants.PER_VERSION_MAPPINGS_NAME, "named");

        this.getAdditionalMappings().putAll(JAVAX_TO_JETBRAINS);
    }
}
