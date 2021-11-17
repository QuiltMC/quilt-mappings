package quilt.internal.decompile.javadoc;

public interface MethodJavadocProvider {
    String provide(String methodName, String descriptor, String owner, int access);
}
