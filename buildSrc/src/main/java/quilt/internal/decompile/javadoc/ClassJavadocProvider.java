package quilt.internal.decompile.javadoc;

public interface ClassJavadocProvider {
    String provide(String className, int access);
}
