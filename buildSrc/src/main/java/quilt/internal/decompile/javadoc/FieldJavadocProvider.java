package quilt.internal.decompile.javadoc;

public interface FieldJavadocProvider {
    String provide(String fieldName, String descriptor, String owner, int access);
}
