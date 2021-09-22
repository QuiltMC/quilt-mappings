package quilt.internal.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import groovy.io.FileType;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;

public class JarRemapper {
    public static void mapJar(File output, File input, File mappings, File libraries, String from, String to) {
        if (output.exists()) {
            output.delete();
        }

        TinyRemapper.Builder remapperBuilder = TinyRemapper.newRemapper()
                .withMappings(TinyUtils.createTinyMappingProvider(mappings.toPath(), from, to))
                .renameInvalidLocals(true)
                .rebuildSourceFilenames(true)
                .ignoreConflicts(true)
                .invalidLvNamePattern(Pattern.compile("\\$\\$\\d+"));
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
