package quilt.internal;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TasksTest {
    @TempDir(cleanup = CleanupMode.NEVER)
    private static Path testProjectDir;

    @BeforeAll
    public static void setup() throws Exception {
        // DEBUG
        System.out.println("Setting up test project at " + testProjectDir.toAbsolutePath());
        Path source = Path.of(TasksTest.class.getResource("/testProject").toURI());

        try (Stream<Path> files = Files.walk(source)) {
            files.forEach(p -> {
                Path relative = source.relativize(p);
                try {
                    if (!Files.isDirectory(relative)) {
                        Files.copy(p, testProjectDir.resolve(relative));
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to copy " + relative, e);
                }
            });
        }
    }

    @Test
    public void testBuildMappingsTiny() throws Exception {
        // TODO
    }
}
