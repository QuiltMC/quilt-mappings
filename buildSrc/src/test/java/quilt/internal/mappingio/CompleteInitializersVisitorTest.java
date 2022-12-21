package quilt.internal.mappingio;

import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import quilt.internal.TestUtil;

import java.nio.file.Path;

public class CompleteInitializersVisitorTest {
    public static final Path FILE = Path.of("src/test/resources/completeInitializers.tiny");

    @Test
    public void test() throws Exception {
        MemoryMappingTree originalTree = TestUtil.readTinyV2(FILE);

        MappingTree completeTree = new MemoryMappingTree();
        originalTree.accept(new CompleteInitializersVisitor((MappingVisitor) completeTree));

        Assertions.assertEquals(originalTree.getNamespaceName(1), "test");
        Assertions.assertNull(((MappingTree) originalTree).getClass("a").getMethod("<init>", "()V").getDstName(1));

        Assertions.assertEquals(completeTree.getNamespaceName(1), "test");
        Assertions.assertEquals(completeTree.getClass("a").getMethod("<init>", "()V").getDstName(1), "<init>");
    }
}
