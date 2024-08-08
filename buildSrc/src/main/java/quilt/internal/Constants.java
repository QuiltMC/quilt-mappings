package quilt.internal;

public class Constants {
    public static final String MINECRAFT_VERSION = "1.21.1";

    public static final String MAPPINGS_NAME = "quilt-mappings";

    public static final String PER_VERSION_MAPPINGS_NAME = "hashed";

    public static final String MAPPINGS_VERSION = MINECRAFT_VERSION + "+build." + System.getenv().getOrDefault("BUILD_NUMBER", "local");

    public static final class Groups {
        public static final String SETUP_GROUP = "jar setup";
        public static final String MAPPINGS_GROUP = MAPPINGS_NAME;
        public static final String BUILD_MAPPINGS_GROUP = "build mappings";
        public static final String MAP_JAR_GROUP = "jar mapping";
        public static final String DECOMPILE_GROUP = "decompile";
        public static final String UNPICK = "unpick";
        public static final String LINT_GROUP = "lint";
        public static final String UNPICK_GEN = "unpick gen";
    }
}
