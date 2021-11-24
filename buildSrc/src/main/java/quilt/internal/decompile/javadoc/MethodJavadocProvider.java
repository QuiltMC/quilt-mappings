package quilt.internal.decompile.javadoc;

public interface MethodJavadocProvider {
    MethodJavadocProvider EMPTY = (methodName, descriptor, owner) -> null;

    String provide(String methodName, String descriptor, String owner);
}
