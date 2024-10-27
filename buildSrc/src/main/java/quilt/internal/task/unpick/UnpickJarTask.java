package quilt.internal.task.unpick;

import java.util.List;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import quilt.internal.Constants.Groups;
import quilt.internal.plugin.MapV2Plugin;
import quilt.internal.task.MappingsTask;

/**
 * Unpicks a jar file using {@link daomephsta.unpick.cli.Main}.
 *
 * @see MapV2Plugin MapV2Plugin's configuration
 */
public abstract class UnpickJarTask extends JavaExec implements MappingsTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapV2Plugin}.
     */
    public static final String UNPICK_HASHED_JAR_TASK_NAME = "unpickHashedJar";

    @InputFile
    public abstract RegularFileProperty getInputFile();

    @InputFile
    public abstract RegularFileProperty getUnpickDefinition();

    @InputFile
    public abstract RegularFileProperty getUnpickConstantsJar();

    @InputFiles
    public abstract ConfigurableFileCollection getDecompileClasspathFiles();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    public UnpickJarTask() {
        this.setGroup(Groups.UNPICK);
        // TODO see if daomephsta.unpick.cli.Main can be added to the classpath here, directly,
        //  eliminating the need for the unpick configuration

        this.getMainClass().set(daomephsta.unpick.cli.Main.class.getName());
        this.getMainClass().finalizeValue();
    }

    @Override
    public void exec() {
        this.args(List.of(
            this.getInputFile().get().getAsFile().getAbsolutePath(),
            this.getOutputFile().get().getAsFile().getAbsolutePath(),
            this.getUnpickDefinition().get().getAsFile().getAbsolutePath(),
            this.getUnpickConstantsJar().get().getAsFile().getAbsolutePath()
        ));

        this.args(this.getDecompileClasspathFiles().getAsFileTree().getFiles());
        super.exec();
    }
}
