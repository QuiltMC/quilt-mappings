package quilt.internal;

public class Constants {
    // TODO move this to libs.versions.toml and pass it to QuiltMappingsExtension for use in QuiltMappingsPlugin
    //  or make QuiltMappingsExtension retrieve it directly from libs so it can be a plain String
    public static final String MINECRAFT_VERSION = "1.21.3";

    public static final String MAPPINGS_NAME = "quilt-mappings";

    public static final String PER_VERSION_MAPPINGS_NAME = "hashed";

    public static final String INTERMEDIARY_MAPPINGS_NAME = "intermediary";

    // TODO why does this use a system variable? CI/CD?
    //  Could it go in gradle.properties instead?
    public static final String MAPPINGS_VERSION = MINECRAFT_VERSION + "+build." +
        System.getenv().getOrDefault("BUILD_NUMBER", "local");

    public static final String UNPICK_NAME = "unpick";

    public interface Groups {
        String SETUP = "jar setup";
        String MAPPINGS = MAPPINGS_NAME;
        String BUILD_MAPPINGS = "build mappings";
        String MAP_JAR = "jar mapping";
        String DECOMPILE = "decompile";
        String UNPICK = UNPICK_NAME;
        String LINT = "lint";
        String UNPICK_GEN = UNPICK_NAME + " gen";
        String DIFF = "diff";
        String JAVADOC_GENERATION = "javadoc generation";
        String PER_VERSION = PER_VERSION_MAPPINGS_NAME;
    }

    public interface Namespaces {
        String INTERMEDIARY = INTERMEDIARY_MAPPINGS_NAME;
        String PER_VERSION = PER_VERSION_MAPPINGS_NAME;
        String NAMED = "named";
        String OFFICIAL = "official";
    }
}
