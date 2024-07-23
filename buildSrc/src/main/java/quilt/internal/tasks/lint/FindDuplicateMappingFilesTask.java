package quilt.internal.tasks.lint;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.nio.file.Path;
import java.util.stream.Stream;

public abstract class FindDuplicateMappingFilesTask extends DefaultTask {
    public static final String TASK_NAME = "findDuplicateMappingFiles";

    private static final Pattern CLASS_HASH_PATTERN = Pattern.compile("^CLASS net/minecraft/unmapped/C_\\w+");

    @InputDirectory
    public abstract DirectoryProperty getMappingDirectory();

    @TaskAction
    public void run() {
        final Multimap<String, File> allMappings = HashMultimap.create();
        final Set<String> multiMappings = new HashSet<>();

        try (Stream<Path> mappingPaths = Files.walk(getMappingDirectory().get().getAsFile().toPath())) {
            mappingPaths.map(Path::toFile)
                .filter(File::isFile)
                .forEach(mappingFile -> {
                    try (var reader = new BufferedReader(new FileReader(mappingFile))) {
                        final String firstLine = reader.readLine();
                        if (firstLine != null) {
                            final var classHashMatcher = CLASS_HASH_PATTERN.matcher(firstLine);
                            if (classHashMatcher.find()) {
                                final String classHash = classHashMatcher.group(0);
                                final Collection<File> classMappings = allMappings.get(classHash);

                                if (!classMappings.isEmpty()) multiMappings.add(classHash);

                                classMappings.add(mappingFile);
                            }
                        }
                    } catch (IOException e) {
                        throw new GradleException("Unexpected error accessing mapping file", e);
                    }
                });
        } catch (IOException e) {
            throw new GradleException("Unexpected error accessing mappings directory", e);
        }

        if (!multiMappings.isEmpty()) {
            final var err = new StringBuilder("Found classes mapped by multiple files!");
            err.append(System.lineSeparator());
            for (final String multiMapping : multiMappings) {
                err.append('\t').append(multiMapping).append(" is mapped by:");
                err.append(System.lineSeparator());

                for (final File mappingFile : allMappings.get(multiMapping)) {
                    err.append("\t\t").append(mappingFile);
                    err.append(System.lineSeparator());
                }
            }

            throw new GradleException(err.toString());
        }
    }
}
