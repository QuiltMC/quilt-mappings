package quilt.internal.task.jarmapping;

import org.gradle.api.tasks.TaskContainer;
import quilt.internal.constants.Groups;
import quilt.internal.constants.Namespaces;
import quilt.internal.plugin.MapMinecraftJarsPlugin;
import quilt.internal.plugin.MapV2Plugin;

/**
 * @see MapMinecraftJarsPlugin MapMinecraftJarsPlugin's configureEach
 */
public abstract class MapNamedJarTask extends MapJarTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapV2Plugin}.
     */
    public static final String MAP_NAMED_JAR_TASK_NAME = "mapNamedJar";

    public MapNamedJarTask() {
        super(Groups.MAP_JAR, Namespaces.PER_VERSION, Namespaces.NAMED);

        this.getAdditionalMappings().putAll(JAVAX_TO_JETBRAINS);
    }
}
