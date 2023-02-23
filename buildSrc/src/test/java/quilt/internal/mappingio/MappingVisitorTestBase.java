package quilt.internal.mappingio;

import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import quilt.internal.util.MappingAssertions;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

public abstract class MappingVisitorTestBase extends MappingAssertions {
    public static Path file(String name) {
        return Path.of("src/test/resources/%s".formatted(name));
    }

    public static MappingTree visitTree(MappingTree tree, Function<MappingVisitor, MappingVisitor> visitor) throws IOException {
        MemoryMappingTree result = new MemoryMappingTree();
        tree.accept(visitor.apply(result));
        return result;
    }
}
