package quilt.internal.mappingio;

import net.fabricmc.mappingio.tree.MappingTree;
import org.junit.jupiter.api.Test;
import quilt.internal.TestUtil;

import java.nio.file.Path;

public class CompleteInitializersVisitorTest extends MappingVisitorTestBase {
    public static final Path FILE = file("completeInitializers.tiny");

    @Test
    public void test() throws Exception {
        MappingTree originalTree = TestUtil.readTinyV2(FILE);
        MappingTree completeTree = visitTree(originalTree, CompleteInitializersVisitor::new);

        assertDstNamespace("test", originalTree, 1);
        assertMappingEmpty(originalTree.getClass("a").getMethod("<init>", "()V"), 1);

        assertDstNamespace("test", completeTree, 1);
        assertMapping("<init>", completeTree.getClass("a").getMethod("<init>", "()V"), 1);
    }
}
