package quilt.internal.task.jarmapping;

import org.gradle.api.tasks.TaskContainer;
import quilt.internal.constants.Groups;
import quilt.internal.constants.Namespaces;
import quilt.internal.plugin.MapMinecraftJarsPlugin;

/**
 * @see MapMinecraftJarsPlugin MapMinecraftJarsPlugin's configureEach
 */
public abstract class MapPerVersionMappingsJarTask extends MapJarTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapMinecraftJarsPlugin}.
     */
    public static final String MAP_INTERMEDIATE_MAPPINGS_JAR_TASK_NAME = "mapIntermediateMappingsJar";

    public MapPerVersionMappingsJarTask() {
        super(Groups.MAP_JAR, Namespaces.OFFICIAL, Namespaces.INTERMEDIATE);
    }
}
