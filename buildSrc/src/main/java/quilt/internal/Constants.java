package quilt.internal;

public class Constants {
    public static final String MINECRAFT_VERSION = "21w41a";

    public static final String MAPPINGS_NAME = "quilt-mappings";

    public static final String PER_VERSION_MAPPINGS_NAME = "hashed";

    public static final String MAPPINGS_VERSION = MINECRAFT_VERSION + "+build." + System.getenv().getOrDefault("BUILD_NUMBER", "local");

    public static final class Groups {
        public static final String SETUP_GROUP = "jar setup";
        public static final String MAPPINGS_GROUP = MAPPINGS_NAME;
        public static final String BUILD_MAPPINGS_GROUP = "mappings build";
        public static final String MAP_JAR_GROUP = "jar mapping";
    }
}
