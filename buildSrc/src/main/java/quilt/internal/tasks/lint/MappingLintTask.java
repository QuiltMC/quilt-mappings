package quilt.internal.tasks.lint;

import cuchaz.enigma.Enigma;
import cuchaz.enigma.EnigmaProject;
import cuchaz.enigma.ProgressListener;
import cuchaz.enigma.analysis.index.EntryIndex;
import cuchaz.enigma.classprovider.ClasspathClassProvider;
import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.mapping.serde.MappingParseException;
import cuchaz.enigma.translation.mapping.serde.enigma.EnigmaMappingsReader;
import cuchaz.enigma.translation.mapping.tree.EntryTree;
import cuchaz.enigma.translation.representation.AccessFlags;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileType;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.ChangeType;
import org.gradle.work.FileChange;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public abstract class MappingLintTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "mappingLint";
    private final DirectoryProperty mappingDirectory = getProject().getObjects().directoryProperty();
    private final RegularFileProperty jarFile;

    public MappingLintTask() {
        super(Constants.Groups.LINT_GROUP);
        // Ignore outputs for up-to-date checks as there aren't any (so only inputs are checked)
        getOutputs().upToDateWhen(task -> true);
        jarFile = getProject().getObjects().fileProperty();
        getCheckers().set(Checker.DEFAULT_CHECKERS);
    }

    @Incremental
    @InputDirectory
    public DirectoryProperty getMappingDirectory() {
        return mappingDirectory;
    }

    @InputFile
    public RegularFileProperty getJarFile() {
        return jarFile;
    }

    @Input
    public abstract SetProperty<Checker<Entry<?>>> getCheckers();

    @Inject
    public abstract WorkerExecutor getWorkerExecutor();

    @TaskAction
    public void run(InputChanges changes) {
        WorkQueue workQueue = getWorkerExecutor().noIsolation();

        workQueue.submit(LintAction.class, parameters -> {
            parameters.getGameJar().set(getJarFile());
            parameters.getCheckers().set(getCheckers());

            for (FileChange change : changes.getFileChanges(getMappingDirectory())) {
                if (change.getChangeType() != ChangeType.REMOVED && change.getFileType() == FileType.FILE) {
                    parameters.getMappingFiles().from(change.getFile());
                }
            }
        });

        workQueue.await();
    }

    private static EntryTree<EntryMapping> readMappings(FileCollection files) throws IOException, MappingParseException {
        Path[] paths = files.getFiles().stream().map(File::toPath).toArray(Path[]::new);
        return EnigmaMappingsReader.readFiles(ProgressListener.none(), paths);
    }

    private static String getFullName(EntryTree<EntryMapping> mappings, Entry<?> entry) {
        String name = mappings.get(entry).targetName();

        if (name == null) {
            name = "<anonymous>";
        }

        if (entry instanceof MethodEntry method) {
            name += method.getDesc().toString();
        }

        if (entry.getParent() != null) {
            name = getFullName(mappings, entry.getParent()) + '.' + name;
        }

        return name;
    }

    public interface LintParameters extends WorkParameters {
        ConfigurableFileCollection getMappingFiles();
        RegularFileProperty getGameJar();
        SetProperty<Checker<Entry<?>>> getCheckers();
    }

    public abstract static class LintAction implements WorkAction<LintParameters> {
        private static final Logger LOGGER = Logging.getLogger(LintAction.class);

        @Inject
        public LintAction() {
        }

        @Override
        public void execute() {
            try {
                LintParameters params = getParameters();
                Set<Checker<Entry<?>>> checkers = getParameters().getCheckers().get();
                Map<Severity, List<String>> messagesBySeverity = new EnumMap<>(Severity.class);

                // Set up Enigma
                Enigma enigma = Enigma.create();
                EnigmaProject project = enigma.openJar(params.getGameJar().get().getAsFile().toPath(), new ClasspathClassProvider(), ProgressListener.none());
                EntryTree<EntryMapping> mappings = readMappings(getParameters().getMappingFiles());
                project.setMappings(mappings);
                Function<Entry<?>, AccessFlags> accessProvider = entry -> {
                    EntryIndex index = project.getJarIndex().getEntryIndex();

                    if (entry instanceof ClassEntry c) {
                        return index.getClassAccess(c);
                    } else {
                        return index.getEntryAccess(entry);
                    }
                };

                mappings.getAllEntries().parallel().forEach(entry -> {
                    EntryMapping mapping = mappings.get(entry);
                    assert mapping != null;
                    List<CheckerError> localErrors = new ArrayList<>();

                    for (Checker<Entry<?>> checker : checkers) {
                        checker.check(entry, mapping, accessProvider, (severity, message) -> localErrors.add(new CheckerError(severity, message)));
                    }

                    if (!localErrors.isEmpty()) {
                        String name = getFullName(mappings, entry);

                        for (CheckerError error : localErrors) {
                            messagesBySeverity.computeIfAbsent(error.severity(), s -> new ArrayList<>()).add(name + ": " + error.message());
                        }
                    }
                });

                if (!messagesBySeverity.isEmpty()) {
                    int errors = 0;
                    int warnings = 0;

                    for (var entry : messagesBySeverity.entrySet()) {
                        if (entry.getKey() == Severity.ERROR) {
                            for (String message : entry.getValue()) {
                                errors++;
                                LOGGER.error("error: {}", message);
                            }
                        } else {
                            for (String message : entry.getValue()) {
                                warnings++;
                                LOGGER.warn("warning: {}", message);
                            }
                        }
                    }

                    String message = String.format("Found %d errors and %d warnings!", errors, warnings);
                    LOGGER.warn(message);

                    if (errors > 0) {
                        throw new GradleException(message + " See the log for details.");
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (MappingParseException e) {
                throw new GradleException("Could not parse mappings", e);
            }
        }

        private record CheckerError(Severity severity, String message) {
        }
    }
}
