package quilt.internal.constants;

public interface Classifiers {
    String INTERMEDIATE = Namespaces.INTERMEDIATE;
    String NAMED = Namespaces.NAMED;
    String INTERMEDIARY = Namespaces.INTERMEDIARY;

    String V2 = "v2";
    String MERGED_V2 = "merged" + V2;
    String INTERMEDIARY_V2 = INTERMEDIARY + "-" + V2;
    String INTERMEDIARY_V2_MERGED = INTERMEDIARY + "-" + MERGED_V2;

    String UNPICKED = "unpicked";
    String INTERMEDIATE_UNPICKED = INTERMEDIATE + "-" + UNPICKED;

    String TINY = Constants.TINY_NAME;

    String CONSTANTS = "constants";
    String JAVADOC = "javadoc";
    String SOURCES = "sources";
}
