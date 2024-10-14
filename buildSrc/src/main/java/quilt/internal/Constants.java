package quilt.internal;

public class Constants {
    // TODO move this to libs.versions.toml and pass it to QuiltMappingsExtension for use in QuiltMappingsPlugin
    //  or make QuiltMappingsExtension retrieve it directly from libs so it can be a plain String
    public static final String MINECRAFT_VERSION = "1.21.2-pre3";

    public static final String MAPPINGS_NAME = "quilt-mappings";

    public static final String PER_VERSION_MAPPINGS_NAME = "hashed";

    public static final String PER_VERSION_INVERTED_MAPPINGS_NAME = PER_VERSION_MAPPINGS_NAME + "-inverted";

    // TODO replace duplicates with this
    public static final String INTERMEDIARY_MAPPINGS_NAME = "intermediary";

    // TODO why does this use a system variable? CI/CD?
    //  Could it go in gradle.properties instead?
    public static final String MAPPINGS_VERSION = MINECRAFT_VERSION + "+build." +
        System.getenv().getOrDefault("BUILD_NUMBER", "local");

    public static final class Groups {
        public static final String SETUP = "jar setup";
        public static final String MAPPINGS = MAPPINGS_NAME;
        public static final String BUILD_MAPPINGS = "build mappings";
        public static final String MAP_JAR = "jar mapping";
        public static final String DECOMPILE = "decompile";
        public static final String UNPICK = "unpick";
        public static final String LINT = "lint";
        public static final String UNPICK_GEN = "unpick gen";
    }
}
