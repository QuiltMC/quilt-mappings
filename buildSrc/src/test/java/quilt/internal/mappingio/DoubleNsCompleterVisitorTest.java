package quilt.internal.mappingio;

import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.junit.jupiter.api.Test;
import quilt.internal.TestUtil;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class DoubleNsCompleterVisitorTest {
    public static final Path FILE = Path.of("src/test/resources/doubleNsCompleter.tiny");

    @Test
    public void test() throws Exception {
        MemoryMappingTree originalTree = TestUtil.readTinyV2(FILE);

        MappingTree visitedTree = new MemoryMappingTree();
        originalTree.accept(new DoubleNsCompleterVisitor((MappingVisitor) visitedTree, "test", "cond", "src"));

        assertEquals("src", visitedTree.getSrcNamespace());
        assertEquals("test", visitedTree.getDstNamespaces().get(1));

        assertEquals("com/a", visitedTree.getClass("a").getName(1));
        assertEquals("a", visitedTree.getField("a", "a", "I").getName(1));
        assertEquals("a", visitedTree.getMethod("a", "a", "()V").getName(1));

        assertNull(visitedTree.getClass("b").getName(1));
        assertNull(visitedTree.getField("b", "a", "I").getName(1));
        assertNull(visitedTree.getMethod("b", "a", "()V").getName(1));

        assertEquals("c", visitedTree.getClass("c").getName(1));
        assertEquals("a", visitedTree.getField("c", "a", "I").getName(1));
        assertEquals("a", visitedTree.getMethod("c", "a", "()V").getName(1));
    }
}
