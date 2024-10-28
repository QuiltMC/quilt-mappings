package quilt.internal.task.diff;

import org.gradle.api.GradleException;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.options.Option;
import quilt.internal.Constants.Groups;
import quilt.internal.plugin.TargetDiffPlugin;
import quilt.internal.task.MappingsTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static quilt.internal.util.ProviderUtil.toOptional;

/**
 * Takes the {@value DIFF_COMMAND} between the contents of two directories and saves the output to a
 * {@linkplain #getDest() destination} file.<br>
 * Both directories ({@link #getFirst() first} and {@link #getSecond() second}) must be non-empty.
 * <p>
 * Properties may be specified on the command line when invoking the task.
 * <p>
 * Requires the <a href="https://www.gnu.org/software/diffutils/">{@value DIFF_COMMAND_PHRASE}</a> be available to
 * command line processes, usually via the {@code PATH} system environment variable.
 */
@CacheableTask
public abstract class DiffDirectoriesTask extends Exec implements MappingsTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link TargetDiffPlugin}.
     */
    public static final String GENERATE_DIFF_TASK_NAME = "generateDiff";

    public static final String DIFF_COMMAND = "diff";

    private static final String DIFF_COMMAND_PHRASE = DIFF_COMMAND + " command";

    @Option(
        option = "args",
        description = "Additional args passed to the " + DIFF_COMMAND_PHRASE + "."
    )
    @Optional
    @Input
    public abstract ListProperty<String> getAdditionalArgs();

    @Option(
        option = "first",
        description = "The first file passed to the " + DIFF_COMMAND_PHRASE + "."
    )
    // required because Exec has @DisableCachingByDefault but this has @CacheableTask
    @PathSensitive(PathSensitivity.ABSOLUTE)
    @InputDirectory
    public abstract DirectoryProperty getFirst();

    @Option(
        option = "second",
        description = "The second file passed to the " + DIFF_COMMAND_PHRASE + "."
    )
    // required because Exec has @DisableCachingByDefault but this has @CacheableTask
    @PathSensitive(PathSensitivity.ABSOLUTE)
    @InputDirectory
    public abstract DirectoryProperty getSecond();

    @Option(
        option = "dest",
        description = "The location to save the " + DIFF_COMMAND_PHRASE + " output to."
    )
    @OutputFile
    public abstract RegularFileProperty getDest();

    public DiffDirectoriesTask() {
        this.setGroup(Groups.DIFF);

        this.setExecutable(DIFF_COMMAND);

        // exit value 1 means there was a difference between the inputs, so we do our own check
        this.setIgnoreExitValue(true);

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

        super.exec();

        final int exitValue = this.getExecutionResult().get().getExitValue();
        switch (exitValue) {
            case 0 -> this.getLogger().lifecycle(":no difference");
            case 1 -> { }
            default -> throw new GradleException(
                "Process 'command '" + DIFF_COMMAND + "'' finished with unexpected exit value " + exitValue
            );
        }
    }
}
