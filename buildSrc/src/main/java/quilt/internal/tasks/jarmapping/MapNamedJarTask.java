package quilt.internal.tasks.jarmapping;

import quilt.internal.Constants.Groups;
import quilt.internal.Constants.Namespaces;

public abstract class MapNamedJarTask extends MapJarTask {
    public static final String MAP_NAMED_JAR_TASK_NAME = "mapNamedJar";

    public MapNamedJarTask() {
        super(Groups.MAP_JAR, Namespaces.PER_VERSION, Namespaces.NAMED);

        this.getAdditionalMappings().putAll(JAVAX_TO_JETBRAINS);
    }
}
