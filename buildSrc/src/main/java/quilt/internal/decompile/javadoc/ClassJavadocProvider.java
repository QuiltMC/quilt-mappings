package quilt.internal.decompile.javadoc;

public interface ClassJavadocProvider {
    ClassJavadocProvider EMPTY = (className, isRecord) -> null;

    String provideClassJavadoc(String className, boolean isRecord);
}
