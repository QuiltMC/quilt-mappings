package quilt.internal.decompile.javadoc;

public interface FieldJavadocProvider {
    FieldJavadocProvider EMPTY = (fieldName, descriptor, owner) -> null;

    String provide(String fieldName, String descriptor, String owner);
}
