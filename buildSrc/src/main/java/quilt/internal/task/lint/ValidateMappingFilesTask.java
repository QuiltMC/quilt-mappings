package quilt.internal.task.lint;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileType;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.ChangeType;
import org.gradle.work.FileChange;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;
import quilt.internal.constants.Extensions;
import quilt.internal.constants.Groups;
import quilt.internal.plugin.MapMinecraftJarsPlugin;
import quilt.internal.plugin.QuiltMappingsBasePlugin;
import quilt.internal.task.MappingsDirConsumingTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.nio.file.Path;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Searches the passed {@link #getMappingsDir() mappingsDir} for any mappings files that map the same class.<br>
 * The task fails if any duplicates or other errors are found.
 * <p>
 * Duplicate mappings are usually the result of running {@code git merge/rebase} and
 * inadvertently combining two histories that give the same class two different names.
 * <p>
 * Any file that's empty, lacks the {@value Extensions#MAPPING} extension,
 * or doesn't begin with a class mapping will also be reported.
 *
 * @see QuiltMappingsBasePlugin QuiltMappingsBasePlugin's configureEach
 */
public abstract class ValidateMappingFilesTask extends DefaultTask implements MappingsDirConsumingTask {
    /**
     * {@linkplain org.gradle.api.tasks.TaskContainer#register Registered} by {@link MapMinecraftJarsPlugin}.
     */
    public static final String VALIDATE_MAPPING_FILES_TASK_NAME = "validateMappingFiles";

    private static final Pattern EXPECTED_CLASS =
        Pattern.compile("(?<=^CLASS )(?:net/minecraft|com/mojang/blaze3d)/(?:\\w+/)*\\w+");

    private static final Gson GSON = new Gson();
    private static final Collector<FileChange, ?, List<File>> CHANGE_TO_FILE_LIST_COLLECTOR = Collector.of(
        ArrayList::new,
        (list, change) -> list.add(change.getFile()),
        (left, right) -> {
            left.addAll(right);
            return left;
        }
    );

    @Incremental
    @InputDirectory
    public abstract DirectoryProperty getMappingsDir();

    @OutputFile
    public abstract RegularFileProperty getValidMappingCache();

    public ValidateMappingFilesTask() {
        this.setGroup(Groups.CHECK_MAPPINGS);
    }

    @TaskAction
    public void run(InputChanges changes) {
        final File mappingsDir = this.getMappingsDir().get().getAsFile();

        final Multimap<String, File> allMappings = HashMultimap.create();

        final File cacheFile = this.getValidMappingCache().get().getAsFile();
        final Consumer<File> removeIfCached;

        {
            final BiMap<String, File> cache = this.readCache(cacheFile, mappingsDir);
            cache.forEach(allMappings::put);

            removeIfCached = file -> {
                final String cachedClassName = cache.inverse().get(file);
                if (cachedClassName != null) {
                    allMappings.remove(cachedClassName, file);
                }
            };
        }

        final Set<String> duplicateMappings = new HashSet<>();
        final List<File> malformedClassFiles = new ArrayList<>();
        final List<File> emptyFiles = new ArrayList<>();
        final List<File> wrongExtensionFiles = new ArrayList<>();

        final Map<Boolean, List<File>> fileChangesByRemoved = StreamSupport
            .stream(changes.getFileChanges(this.getMappingsDir()).spliterator(), false)
            .filter(change -> change.getFileType() == FileType.FILE)
            .collect(Collectors.partitioningBy(
                change -> change.getChangeType() == ChangeType.REMOVED,
                CHANGE_TO_FILE_LIST_COLLECTOR
            ));

        // handle removals first in case of renames
        fileChangesByRemoved.get(true).forEach(removeIfCached);

        fileChangesByRemoved.get(false).forEach(mappingFile -> {
            removeIfCached.accept(mappingFile);

            try (var reader = new BufferedReader(new FileReader(mappingFile))) {
                final String firstLine = reader.readLine();
                if (firstLine != null) {
                    getClassName(firstLine).ifPresentOrElse(
                        className -> {
                            final Collection<File> classMappings = allMappings.get(className);

                            if (!classMappings.isEmpty()) {
                                duplicateMappings.add(className);
                            }

                            classMappings.add(mappingFile);
                        },
                        () -> malformedClassFiles.add(mappingFile)
                    );
                } else {
                    emptyFiles.add(mappingFile);
                }

                if (!mappingFile.toString().endsWith("." + Extensions.MAPPING)) {
                    wrongExtensionFiles.add(mappingFile);
                }
            } catch (IOException e) {
                this.deleteCache(cacheFile);
                throw new GradleException("Unexpected error accessing " + Extensions.MAPPING + " file", e);
            }
        });

        this.writeCache(allMappings, cacheFile, mappingsDir);

        final Logger logger = this.getLogger();
        final List<String> errorMessages = new ArrayList<>();
        if (!duplicateMappings.isEmpty()) {
            final String message = "%d class%s mapped by multiple files".formatted(
                duplicateMappings.size(),
                duplicateMappings.size() == 1 ? "" : "es"
            );
            errorMessages.add(message);

            logger.error("Found {}!", message);
            for (final String duplicateMapping : duplicateMappings) {
                logger.error("\t{} is mapped by:", duplicateMapping);

                for (final File mappingFile : allMappings.get(duplicateMapping)) {
                    logger.error("\t\t{}", mappingFile);
                }
            }
        }

        if (!malformedClassFiles.isEmpty()) {
            final String message = "%d files with malformed class format%s".formatted(
                malformedClassFiles.size(),
                malformedClassFiles.size() == 1 ? "" : "s"
            );

            errorMessages.add(message);

            logger.error("Found {}!", message);
            for (final File malformedClassFile : malformedClassFiles) {
                logger.error("\t{}", malformedClassFile);
            }
        }

        if (!emptyFiles.isEmpty()) {
            final String message = "%d empty file%s".formatted(
                emptyFiles.size(),
                emptyFiles.size() == 1 ? "" : "s"
            );

            errorMessages.add(message);

            logger.error("Found {}!", message);
            for (final File emptyFile : emptyFiles) {
                logger.error("\t{}", emptyFile);
            }
        }

        if (!wrongExtensionFiles.isEmpty()) {
            final String message = ("%d file%s without the " + Extensions.MAPPING + " extension").formatted(
                wrongExtensionFiles.size(),
                wrongExtensionFiles.size() == 1 ? "" : "s"
            );

            errorMessages.add(message);

            logger.error("Found {}!", message);
            for (final File wrongExtensionFile : wrongExtensionFiles) {
                logger.error("\t{}", wrongExtensionFile);
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

    private void writeCache(Multimap<String, File> mappings, File cacheFile, File mappingsDir) {
        try (var writer = new FileWriter(cacheFile)) {
            cacheFile.getParentFile().mkdirs();
            cacheFile.createNewFile();
            final Path mappingsPath = mappingsDir.toPath();
            GSON.toJson(
                mappings.asMap().entrySet().stream()
                    .filter(entry -> entry.getValue().size() == 1)
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            final String relativePath = mappingsPath
                                .relativize(entry.getValue().iterator().next().toPath())
                                .toString().replace('\\', '/');
                            return relativePath.substring(0, relativePath.length() - (Extensions.MAPPING.length() + 1));
                        }
                    )),
                writer
            );
        } catch (IOException e) {
            this.getLogger().error("Unexpected error writing cache", e);
            this.deleteCache(cacheFile);
        }
    }

    private BiMap<String, File> readCache(File cacheFile, File mappingsDir) {
        final Path mappingsPath = mappingsDir.toPath();

        if (cacheFile.exists()) {
            try (var reader = new JsonReader(new FileReader(cacheFile))) {
                return GSON.<Map<String, String>>fromJson(reader, new TypeToken<Map<String, String>>() { }.getType())
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> mappingsPath.resolve(entry.getValue() + "." + Extensions.MAPPING).toFile(),
                        (left, right) -> {
                            throw new IllegalArgumentException(
                                "Duplicate class name for files:\n\t%s\n\t%s".formatted(left, right)
                            );
                        },
                        HashBiMap::create
                    ));
            } catch (IOException | IllegalArgumentException e) {
                this.getLogger().error("Unexpected error reading cache; clearing", e);
                return HashBiMap.create();
            }
        } else {
            return HashBiMap.create();
        }
    }

    private void deleteCache(File cacheFile) {
        this.getLogger().error("Deleting cache");
        cacheFile.delete();
    }

    private static Optional<String> getClassName(String line) {
        final var matcher = EXPECTED_CLASS.matcher(line);
        return matcher.find() ?
            Optional.of(matcher.group(0)) :
            Optional.empty();
    }
}
