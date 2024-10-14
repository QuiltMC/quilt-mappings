package quilt.internal.tasks.setup;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public abstract class ExtractServerJarTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "extractServerJar";

    @InputFile
    public abstract RegularFileProperty getServerBootstrapJar();

    @OutputFile
    public abstract RegularFileProperty getServerJar();

    public ExtractServerJarTask() {
        super(Constants.Groups.SETUP);
    }

    @TaskAction
    public void extractServerJar() throws IOException {
        FileUtils.copyFile(
            // TODO eliminate project access in task action
            this.getProject()
                .zipTree(this.getServerBootstrapJar().get())
                .matching(patternFilterable -> patternFilterable.include("META-INF/versions/*/server-*.jar"))
                .getSingleFile(),
            this.getServerJar().get().getAsFile()
        );
    }
}
