package quilt.internal.decompile.javadoc;

public interface ClassJavadocProvider {
    ClassJavadocProvider EMPTY = (className, isRecord) -> null;

    String provide(String className, boolean isRecord);
}
