package quilt.internal.decompile.javadoc;

public interface MethodJavadocProvider extends JavadocProvider {
    MethodJavadocProvider EMPTY = (methodName, descriptor, owner) -> null;

    String provideMethodJavadoc(String methodName, String descriptor, String owner);
}
