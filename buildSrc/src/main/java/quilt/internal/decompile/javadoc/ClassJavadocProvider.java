package quilt.internal.decompile.javadoc;

public interface ClassJavadocProvider extends JavadocProvider {
    ClassJavadocProvider EMPTY = (className, isRecord) -> null;

    String provideClassJavadoc(String className, boolean isRecord);
}
