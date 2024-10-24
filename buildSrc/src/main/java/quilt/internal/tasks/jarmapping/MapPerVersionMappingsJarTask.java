package quilt.internal.tasks.jarmapping;

import org.gradle.api.tasks.TaskContainer;
import quilt.internal.Constants.Groups;
import quilt.internal.Constants.Namespaces;
import quilt.internal.plugin.MapMinecraftJarsPlugin;

public abstract class MapPerVersionMappingsJarTask extends MapJarTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapMinecraftJarsPlugin}.
     */
    public static final String MAP_PER_VERSION_MAPPINGS_JAR_TASK_NAME = "mapPerVersionMappingsJar";

    public MapPerVersionMappingsJarTask() {
        super(Groups.MAP_JAR, Namespaces.OFFICIAL, Namespaces.PER_VERSION);
    }
}
