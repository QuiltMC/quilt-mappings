package quilt.internal.tasks.jarmapping;

import quilt.internal.Constants.Groups;
import quilt.internal.Constants.Namespaces;

public abstract class MapPerVersionMappingsJarTask extends MapJarTask {
    public static final String MAP_PER_VERSION_MAPPINGS_JAR_TASK_NAME = "mapPerVersionMappingsJar";

    public MapPerVersionMappingsJarTask() {
        super(Groups.MAP_JAR, Namespaces.OFFICIAL, Namespaces.PER_VERSION);
    }
}
