package quilt.internal.constants;

public interface Classifiers {
    String INTERMEDIARY = Namespaces.INTERMEDIARY;
    String PER_VERSION = Namespaces.PER_VERSION;
    String NAMED = Namespaces.NAMED;

    String V2 = "v2";
    String MERGED_V2 = "merged" + V2;
    String INTERMEDIARY_V2 = INTERMEDIARY + "-" + V2;
    String INTERMEDIARY_V2_MERGED = INTERMEDIARY + "-" + MERGED_V2;

    String UNPICKED = "unpicked";
    String PER_VERSION_UNPICKED = PER_VERSION + "-" + UNPICKED;

    String TINY = Constants.TINY_NAME;

    String CONSTANTS = "constants";
    String JAVADOC = "javadoc";
    String SOURCES = "sources";
}
