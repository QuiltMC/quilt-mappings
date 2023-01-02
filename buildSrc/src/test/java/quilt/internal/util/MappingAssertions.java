package quilt.internal.util;

import net.fabricmc.mappingio.tree.MappingTree;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public abstract class MappingAssertions {
    public static void assertSrcNamespace(String expected, MappingTree tree) {
        Assertions.assertEquals(expected, tree.getSrcNamespace());
    }

    public static void assertDstNamespace(String expected, MappingTree tree, int namespace) {
        Assertions.assertEquals(expected, tree.getDstNamespaces().get(namespace));
    }

    public static void assertDstNamespaces(List<String> expected, MappingTree tree) {
        Assertions.assertEquals(expected, tree.getDstNamespaces());
    }

    public static void assertMapping(String expectedName, MappingTree.ElementMapping actual, String namespace) {
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expectedName, actual.getName(namespace));
    }

    public static void assertMapping(String expectedName, MappingTree.ElementMapping actual, int namespace) {
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expectedName, actual.getName(namespace));
    }

    public static void assertMappingNotEmpty(MappingTree.ElementMapping actual, String namespace) {
        Assertions.assertNotNull(actual);
        String name = actual.getName(namespace);
        Assertions.assertNotNull(name);
        Assertions.assertFalse(name.isEmpty());
    }

    public static void assertMappingNotEmpty(MappingTree.ElementMapping actual, int namespace) {
        Assertions.assertNotNull(actual);
        String name = actual.getName(namespace);
        Assertions.assertNotNull(name);
        Assertions.assertFalse(name.isEmpty());
    }

    public static void assertMappingEmpty(MappingTree.ElementMapping actual, String namespace) {
        Assertions.assertNull(actual.getName(namespace));
    }

    public static void assertMappingEmpty(MappingTree.ElementMapping actual, int namespace) {
        Assertions.assertNull(actual.getName(namespace));
    }

    public static void assertComment(String expected, MappingTree.ElementMapping actual) {
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual.getComment());
    }
}
