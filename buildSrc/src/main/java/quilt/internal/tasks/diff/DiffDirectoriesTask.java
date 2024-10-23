package quilt.internal.tasks.diff;

import org.gradle.api.GradleException;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.specs.Specs;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import quilt.internal.Constants.Groups;
import quilt.internal.tasks.MappingsTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static quilt.internal.util.ProviderUtil.toOptional;

public abstract class DiffDirectoriesTask extends Exec implements MappingsTask {
    public static final String GENERATE_DIFF_TASK_NAME = "generateDiff";

    public static final String DIFF_COMMAND = "diff";

    private static final String EXIT_VALUE_1_ERROR =
        "Process 'command '" + DIFF_COMMAND + "'' finished with non-zero exit value 1";

    @Option(
        option = "args",
        description = "Additional args passed to the " + DIFF_COMMAND + " command."
    )
    @Optional
    @Input
    public abstract ListProperty<String> getAdditionalArgs();

    @Option(
        option = "first",
        description = "The first file passed to the " + DIFF_COMMAND + " command."
    )
    @InputDirectory
    public abstract DirectoryProperty getFirst();

    @Option(
        option = "second",
        description = "The second file passed to the " + DIFF_COMMAND + " command."
    )
    @InputDirectory
    public abstract DirectoryProperty getSecond();

    @Option(
        option = "dest",
        description = "The location to save the " + DIFF_COMMAND + " command output to."
    )
    @OutputFile
    public abstract RegularFileProperty getDest();

    public DiffDirectoriesTask() {
        this.setGroup(Groups.DIFF);

        this.setExecutable(DIFF_COMMAND);

        this.getOutputs().cacheIf(
            "Re-enable caching because Exec has @DisableCachingByDefault",
            Specs.satisfyAll()
        );

        this.getArgumentProviders().add(() -> {
            // require neither directory is empty so the diff isn't just the full contents of one of them
            final Directory first = this.getFirst().get();
            if (first.getAsFileTree().isEmpty()) {
                throw new GradleException("first directory is empty");
            }

            final Directory second = this.getSecond().get();
            if (second.getAsFileTree().isEmpty()) {
                throw new GradleException("second directory is empty");
            }

            final List<String> args = new ArrayList<>();

            toOptional(this.getAdditionalArgs()).ifPresent(args::addAll);

            args.add(first.getAsFile().getAbsolutePath());
            args.add(second.getAsFile().getAbsolutePath());

            return args;
        });
    }

    @Override
    @TaskAction
    public void exec() {
        try {
            final File dest = this.getDest().get().getAsFile();

            dest.getParentFile().mkdirs();

            dest.createNewFile();

            this.setStandardOutput(new FileOutputStream(dest.getAbsolutePath()));
        } catch (IOException e) {
            throw new GradleException("Failed to access destination file", e);
        }

        try {
            super.exec();
        } catch (GradleException e) {
            // ignore exit value 1 which just means there was a difference between the inputs
            if (!e.getMessage().equals(EXIT_VALUE_1_ERROR)) {
                throw new GradleException("Error executing " + DIFF_COMMAND, e);
            }
        }
    }
}
