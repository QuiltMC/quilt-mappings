package quilt.internal.tasks.diff;

import java.util.Map;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import quilt.internal.Constants;
import quilt.internal.tasks.jarmapping.MapJarTask;

public abstract class RemapTargetMinecraftJarTask extends MapJarTask implements UnpickVersionsMatchConsumingTask {
    public static final String TASK_NAME = "remapTargetMinecraftJar";

    // TODO temporarily internal, until CheckTargetVersionExistsTask is converted to a BuildService
    @Internal("temporary")
    @Override
    // @InputFile
    // @Optional
    public abstract RegularFileProperty getInputJar();

    @Input
    @Optional
    public abstract Property<Boolean> getUnpickVersionsMatch();

    public RemapTargetMinecraftJarTask() {
        super("diff", Constants.PER_VERSION_MAPPINGS_NAME, "named");
    }

    public Map<String, String> getAdditionalMappings() {
        return MapJarTask.JAVAX_TO_JETBRAINS;
    }
}
