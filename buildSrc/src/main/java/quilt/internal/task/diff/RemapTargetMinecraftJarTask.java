package quilt.internal.task.diff;

import org.gradle.api.tasks.TaskContainer;
import quilt.internal.constants.Groups;
import quilt.internal.constants.Namespaces;
import quilt.internal.plugin.MapMinecraftJarsPlugin;
import quilt.internal.plugin.TargetDiffPlugin;
import quilt.internal.task.jarmapping.MapJarTask;

/**
 * @see MapMinecraftJarsPlugin MapMinecraftJarsPlugin's configureEach
 * @see TargetDiffPlugin TargetDiffPlugin's configureEach[es]
 */
public abstract class RemapTargetMinecraftJarTask extends MapJarTask implements UnpickVersionsMatchConsumingTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link TargetDiffPlugin}.
     */
    public static final String REMAP_TARGET_MINECRAFT_JAR_TASK_NAME = "remapTargetMinecraftJar";

    public RemapTargetMinecraftJarTask() {
        super(Groups.DIFF, Namespaces.PER_VERSION, Namespaces.NAMED);

        this.getAdditionalMappings().putAll(JAVAX_TO_JETBRAINS);
    }
}
