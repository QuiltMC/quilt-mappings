package quilt.internal.mappingio;

import net.fabricmc.mappingio.tree.MappingTree;
import org.junit.jupiter.api.Test;
import quilt.internal.TestUtil;

import java.nio.file.Path;

public class UnmappedNameRemoverVisitorTest extends MappingVisitorTestBase {
    public static final Path FILE = file("unmappedNameRemover.tiny");

    @Test
    public void test() throws Exception {
        MappingTree originalTree = TestUtil.readTinyV2(FILE);
        MappingTree visitedTree = visitTree(originalTree, v -> new UnmappedNameRemoverVisitor(v, "test", "inter"));

        // Classes
        assertMapping("c/C_001", visitedTree.getClass("a"), "test"); // Since it has a named inner class, it needs to have a name
        assertMapping("c/C_001$Bar", visitedTree.getClass("a$a"), "test");
        assertMapping("c/C_001$Bar$C_003", originalTree.getClass("a$a$a"), "test");
        // assertMappingEmpty(visitedTree.getClass("a$a$a"), "test"); // TODO
        // assertMappingEmpty(visitedTree.getClass("b"), "test");
        assertMapping("c/Example", visitedTree.getClass("c"), "test");

        // Fields
        assertMappingEmpty(visitedTree.getField("a", "a", "I"), "test");
        assertComment("f1", visitedTree.getField("a", "a", "I"));
        assertMappingEmpty(visitedTree.getField("a$a", "a", "J"), "test");
        assertComment("f2", visitedTree.getField("a$a", "a", "J"));
        assertMappingEmpty(visitedTree.getField("b", "a", "C"), "test");
        assertComment("f3", visitedTree.getField("b", "a", "C"));

        // Methods
        MappingTree.MethodMapping m1 = visitedTree.getMethod("a", "a", "()V");
        assertMappingEmpty(m1, "test");
        assertComment("m1", m1);
        MappingTree.MethodMapping m2 = visitedTree.getMethod("a$a$a", "a", "(I)V");
        assertMappingEmpty(m2, "test");
        assertComment("m2", m2);
        assertMapping("i", m2.getArg(-1, 1, null), "test");
        MappingTree.MethodMapping m3 = visitedTree.getMethod("c", "a", "(Z)J");
        assertMappingEmpty(m3, "test");
        assertMapping("bool", m3.getArg(-1, 0, null), "test");
    }
}
