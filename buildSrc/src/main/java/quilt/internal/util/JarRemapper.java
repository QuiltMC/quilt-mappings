package quilt.internal.util;

import java.io.File;
import java.util.regex.Pattern;

import groovy.io.FileType;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.Action;

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
                .invalidLvNamePattern(Pattern.compile("\\$\\$\\d+"));
//        action.execute(remapperBuilder);
        TinyRemapper remapper = remapperBuilder
                .build();

        try {
            OutputConsumerPath outputConsumer = new OutputConsumerPath(output.toPath());
            outputConsumer.addNonClassFiles(input.toPath());
            remapper.readInputs(input.toPath());

            ResourceGroovyMethods.eachFileRecurse(libraries, FileType.FILES, new Closure(null, null) {
                public void doCall(File file) {
                    remapper.readClassPath(file.toPath());
                }
            });
            remapper.apply(outputConsumer);
            outputConsumer.close();
            remapper.finish();
        } catch (Exception e) {
            remapper.finish();
            throw new RuntimeException("Failed to remap jar", e);
        }
    }
}
