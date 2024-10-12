package quilt.internal.tasks.jarmapping;

import quilt.internal.Constants;

public abstract class MapPerVersionMappingsJarTask extends MapJarTask {
    public static final String TASK_NAME = "mapPerVersionMappingsJar";

    public MapPerVersionMappingsJarTask() {
        super(Constants.Groups.MAP_JAR, "official", Constants.PER_VERSION_MAPPINGS_NAME);
    }
}
