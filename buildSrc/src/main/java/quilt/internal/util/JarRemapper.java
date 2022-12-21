package quilt.internal.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import org.gradle.api.Action;

import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyRemapperConfiguration;
import net.fabricmc.tinyremapper.TinyUtils;

public class JarRemapper {
    public static void mapJar(File output, File input, File mappings, File libraries, String from, String to, @Nullable Action<TinyRemapper.Builder> action) {
        mapJar(output.toPath(), input.toPath(), mappings.toPath(), new Path[]{libraries.toPath()}, from, to, action);
    }

    public static void mapJar(Path output, Path input, Path mappings, Path[] libraries, String from, String to, @Nullable Action<TinyRemapper.Builder> action) {
        if (Files.exists(output)) {
            try {
                Files.deleteIfExists(output);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete the existing output file", e);
            }
        }

        TinyRemapper.Builder remapperBuilder = TinyRemapper.newRemapper()
                .withMappings(TinyUtils.createTinyMappingProvider(mappings, from, to))
                .configuration(new TinyRemapperConfiguration(
                        false,
                        true,
                        false,
                        false,
                        false,
                        true,
                        false,
                        true,
                        Pattern.compile("\\$\\$\\d+|c_[a-z]{8}"),
                        false));

        if (action != null) {
            action.execute(remapperBuilder);
        }

        TinyRemapper remapper = remapperBuilder.build();

        try (OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(output).build()) {
            outputConsumer.addNonClassFiles(input);
            remapper.readInputs(input);

            remapper.readClassPath(libraries);

            remapper.apply(outputConsumer);
            remapper.finish();
        } catch (Exception e) {
            remapper.finish();
            throw new RuntimeException("Failed to remap jar", e);
        }
    }
}
