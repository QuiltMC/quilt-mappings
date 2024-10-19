package quilt.internal.tasks.unpick;

import java.util.List;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputFile;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;

public abstract class UnpickJarTask extends JavaExec implements MappingsTask {
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
        this.setGroup(Constants.Groups.UNPICK);

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
