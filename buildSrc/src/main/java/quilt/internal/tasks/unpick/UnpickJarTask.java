package quilt.internal.tasks.unpick;

import java.util.List;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputFile;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;

public abstract class UnpickJarTask extends JavaExec implements MappingsTask {
    @InputFile
    public abstract RegularFileProperty getInputFile();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @InputFile
    public abstract RegularFileProperty getUnpickDefinition();

    @InputFile
    public abstract RegularFileProperty getUnpickConstantsJar();

    public UnpickJarTask() {
        this.setGroup(Constants.Groups.UNPICK);

        this.getMainClass().set("daomephsta.unpick.cli.Main");
        this.classpath(this.getProject().getConfigurations().getByName("unpick"));
    }

    @Override
    public void exec() {
        this.args(List.of(
            this.getInputFile().get().getAsFile().getAbsolutePath(),
            this.getOutputFile().get().getAsFile().getAbsolutePath(),
            this.getUnpickDefinition().get().getAsFile().getAbsolutePath(),
            this.getUnpickConstantsJar().get().getAsFile().getAbsolutePath()
        ));

        this.args(this.getProject().getConfigurations().getByName("decompileClasspath").getFiles());
        super.exec();
    }
}
