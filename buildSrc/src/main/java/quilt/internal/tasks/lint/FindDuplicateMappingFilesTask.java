package quilt.internal.tasks.lint;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;
import java.nio.file.Path;
import java.util.stream.Stream;

public abstract class FindDuplicateMappingFilesTask extends DefaultTask {
    public static final String TASK_NAME = "findDuplicateMappingFiles";

    private static final Logger LOGGER = Logging.getLogger(FindDuplicateMappingFilesTask.class);
    private static final Pattern MINECRAFT_CLASS = Pattern.compile("^CLASS net/minecraft/(?:\\w+/)*\\w+ ");
    private static final Pattern BLAZE_CLASS = Pattern.compile("^CLASS com/mojang/blaze3d/(?:\\w+/)*\\w+ ");

    @InputDirectory
    public abstract DirectoryProperty getMappingDirectory();

    @TaskAction
    public void run() {
        final Multimap<String, File> allMappings = HashMultimap.create();
        final Set<String> duplicateMappings = new HashSet<>();
        final List<File> emptyFiles = new ArrayList<>();
        final List<File> mapNoClassFiles = new ArrayList<>();
        final List<File> wrongExtensionFiles = new ArrayList<>();

        try (Stream<Path> mappingPaths = Files.walk(getMappingDirectory().get().getAsFile().toPath())) {
            mappingPaths.map(Path::toFile)
                .filter(File::isFile)
                .forEach(mappingFile -> {
                    try (var reader = new BufferedReader(new FileReader(mappingFile))) {
                        final String firstLine = reader.readLine();
                        if (firstLine != null) {
                            getClassMatch(firstLine).ifPresentOrElse(
                                classMatch -> {
                                    final Collection<File> classMappings = allMappings.get(classMatch);

                                    if (!classMappings.isEmpty()) duplicateMappings.add(classMatch);

                                    classMappings.add(mappingFile);
                                },
                                () -> mapNoClassFiles.add(mappingFile)
                            );

                            if (!mappingFile.toString().endsWith(".mapping")) {
                                wrongExtensionFiles.add(mappingFile);
                            }
                        } else {
                            emptyFiles.add(mappingFile);
                        }
                    } catch (IOException e) {
                        throw new GradleException("Unexpected error accessing mapping file", e);
                    }
                });
        } catch (IOException e) {
            throw new GradleException("Unexpected error accessing mappings directory", e);
        }

        final List<String> errorMessages = new ArrayList<>();
        if (!duplicateMappings.isEmpty()) {
            final String message = "%d class%s mapped by multiple files".formatted(
                duplicateMappings.size(),
                duplicateMappings.size() == 1 ? "" : "es"
            );
            errorMessages.add(message);

            LOGGER.error("Found {}!", message);
            for (final String duplicateMapping : duplicateMappings) {
                LOGGER.error("\t{} is mapped by:", duplicateMapping);

                for (final File mappingFile : allMappings.get(duplicateMapping)) {
                    LOGGER.error("\t\t{}", mappingFile);
                }
            }
        }

        if (!emptyFiles.isEmpty()) {
            final String message = "%d empty file%s".formatted(
                emptyFiles.size(),
                emptyFiles.size() == 1 ? "" : "s"
            );
            errorMessages.add(message);

            LOGGER.error("Found {}!", message);
            for (final File emptyFile : emptyFiles) {
                LOGGER.error("\t{}", emptyFile);
            }
        }

        if (!mapNoClassFiles.isEmpty()) {
            final String message = "%d file%s not mapping a class".formatted(
                mapNoClassFiles.size(),
                mapNoClassFiles.size() == 1 ? "" : "s"
            );
            errorMessages.add(message);

            LOGGER.error("Found {}!", message);
            for (final File mapNoClassFile : mapNoClassFiles) {
                LOGGER.error("\t{}", mapNoClassFile);
            }
        }

        if (!wrongExtensionFiles.isEmpty()) {
            final String message = "%d file%s without the mapping extension".formatted(
                wrongExtensionFiles.size(),
                wrongExtensionFiles.size() == 1 ? "" : "s"
            );
            errorMessages.add(message);

            LOGGER.error("Found {}!", message);
            for (final File mapNoClassFile : mapNoClassFiles) {
                LOGGER.error("\t{}", mapNoClassFile);
            }
        }

        if (!errorMessages.isEmpty()) {
            final var fullError = new StringBuilder("Found ");
            switch (errorMessages.size()) {
                case 1 -> { }
                case 2 -> fullError.append(errorMessages.getFirst()).append(" and ");
                default -> {
                    final List<String> allButLastMessage = errorMessages.subList(0, errorMessages.size() - 1);
                    for (final String message : allButLastMessage) {
                        fullError.append(message).append(", ");
                    }

                    fullError.append("and ");
                }
            }

            fullError.append(errorMessages.getLast()).append("! See the log for details.");

            throw new GradleException(fullError.toString());
        }
    }

    private static Optional<String> getClassMatch(String firstLine) {
        final var minecraftClassMatcher = MINECRAFT_CLASS.matcher(firstLine);
        if (minecraftClassMatcher.find()) {
            return Optional.of(minecraftClassMatcher.group(0));
        } else {
            final var blazeClassMatcher = BLAZE_CLASS.matcher(firstLine);
            if (blazeClassMatcher.find()) {
                return Optional.of(blazeClassMatcher.group(0));
            } else {
                return Optional.empty();
            }
        }
    }
}
