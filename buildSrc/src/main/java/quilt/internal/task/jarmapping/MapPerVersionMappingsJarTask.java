package quilt.internal.task.jarmapping;

import org.gradle.api.tasks.TaskContainer;
import quilt.internal.Constants;
import quilt.internal.Constants.Groups;
import quilt.internal.Constants.Namespaces;
import quilt.internal.plugin.MapMinecraftJarsPlugin;

/**
 * @see MapMinecraftJarsPlugin MapMinecraftJarsPlugin's configureEach
 */
public abstract class MapPerVersionMappingsJarTask extends MapJarTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapMinecraftJarsPlugin}.
     */
    public static final String MAP_PER_VERSION_MAPPINGS_JAR_TASK_NAME = "mapPerVersionMappingsJar";
    public static final String PER_VERSION_CLASSIFIER = Constants.PER_VERSION_MAPPINGS_NAME;

    public MapPerVersionMappingsJarTask() {
        super(Groups.MAP_JAR, Namespaces.OFFICIAL, Namespaces.PER_VERSION);
    }
}
