package quilt.internal.mappingio;

import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.junit.jupiter.api.Test;
import quilt.internal.TestUtil;

import java.nio.file.Path;

public class DoubleNsCompleterVisitorTest extends MappingVisitorTestBase {
    public static final Path FILE = file("doubleNsCompleter.tiny");

    @Test
    public void test() throws Exception {
        MemoryMappingTree originalTree = TestUtil.readTinyV2(FILE);
        MappingTree visitedTree = visitTree(originalTree, v -> new DoubleNsCompleterVisitor(v, "test", "cond", "src"));

        assertSrcNamespace("src", visitedTree);
        assertDstNamespace("test", visitedTree, 1);

        assertMapping("com/a", visitedTree.getClass("a"), 1);
        assertMapping("a", visitedTree.getField("a", "a", "I"), 1);
        assertMapping("a", visitedTree.getMethod("a", "a", "()V"), 1);

        assertMappingEmpty(visitedTree.getClass("b"), 1);
        assertMappingEmpty(visitedTree.getField("b", "a", "I"), 1);
        assertMappingEmpty(visitedTree.getMethod("b", "a", "()V"), 1);

        assertMapping("c", visitedTree.getClass("c"), 1);
        assertMapping("a", visitedTree.getField("c", "a", "I"), 1);
        assertMapping("a", visitedTree.getMethod("c", "a", "()V"), 1);
    }
}
