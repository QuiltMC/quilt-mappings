package quilt.internal.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import groovy.io.FileType;
import groovy.lang.Closure;
import javax.annotation.Nullable;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.Action;

import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;

public class JarRemapper {
    public static void mapJar(File output, File input, File mappings, File libraries, String from, String to, @Nullable Action<TinyRemapper.Builder> action) {
        if (output.exists()) {
            output.delete();
        }

        TinyRemapper.Builder remapperBuilder = TinyRemapper.newRemapper()
                .withMappings(TinyUtils.createTinyMappingProvider(mappings.toPath(), from, to))
                .renameInvalidLocals(true)
                .rebuildSourceFilenames(true)
                .ignoreConflicts(true)
                .invalidLvNamePattern(Pattern.compile("\\$\\$\\d+"));

        if (action!= null) {
            action.execute(remapperBuilder);
        }

        TinyRemapper remapper = remapperBuilder
                .build();

        try {
            OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(output.toPath()).build();
            outputConsumer.addNonClassFiles(input.toPath());
            remapper.readInputs(input.toPath());

            remapper.readClassPath(libraries.toPath());

            remapper.apply(outputConsumer);
            outputConsumer.close();
            remapper.finish();
        } catch (Exception e) {
            remapper.finish();
            throw new RuntimeException("Failed to remap jar", e);
        }
    }
}
