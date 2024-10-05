package quilt.internal.tasks.unpick;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Reader;
import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Writer;
import javax.inject.Inject;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;
import org.jetbrains.annotations.VisibleForTesting;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.util.UnpickUtil;

public abstract class CombineUnpickDefinitionsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "combineUnpickDefinitions";

    // TODO convert this to a FileCollection
    @InputDirectory
    public abstract DirectoryProperty getInput();

    @OutputFile
    public abstract RegularFileProperty getOutput();

    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    public CombineUnpickDefinitionsTask() {
        super(Constants.Groups.UNPICK);
    }

    @TaskAction
    public void run() {
        final WorkQueue workQueue = this.getWorkerExecutor().noIsolation();
        workQueue.submit(CombineAction.class, parameters -> {
            parameters.getInput().set(this.getInput());
            parameters.getOutput().set(this.getOutput());
        });
    }

    @VisibleForTesting
    public static void combineUnpickDefinitions(Collection<File> input, Path output) {
        try {
            Files.deleteIfExists(output);

            final UnpickV2Writer writer = new UnpickV2Writer();

            // TODO make this use a stream instead
            // Sort inputs to get reproducible outputs (also for testing)
            final List<File> files = new ArrayList<>(input);
            files.sort(Comparator.comparing(File::getName));

            for (final File file : files) {
                if (!file.getName().endsWith(".unpick")) {
                    continue;
                }

                try (UnpickV2Reader reader = new UnpickV2Reader(new FileInputStream(file))) {
                    reader.accept(writer);
                }
            }

            Files.writeString(output, UnpickUtil.getLfOutput(writer));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public interface CombineParameters extends WorkParameters {
        @InputDirectory
        DirectoryProperty getInput();

        @OutputFile
        RegularFileProperty getOutput();
    }

    public abstract static class CombineAction implements WorkAction<CombineParameters> {
        @Inject
        public CombineAction() {
        }

        @Override
        public void execute() {
            final Set<File> input = this.getParameters().getInput().getAsFileTree().getFiles();
            final Path output = this.getParameters().getOutput().getAsFile().get().toPath();
            combineUnpickDefinitions(input, output);
        }
    }
}
