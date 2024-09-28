package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public abstract class ExtractServerJarTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "extractServerJar";

    @OutputFile
    public abstract RegularFileProperty getServerBootstrapJar();

    @OutputFile
    public abstract RegularFileProperty getServerJar();

    public ExtractServerJarTask() {
        super(Constants.Groups.SETUP_GROUP);
        // TODO test if this dependency can be removed because of output file is an input
        this.dependsOn(DownloadMinecraftJarsTask.TASK_NAME);

        this.getServerBootstrapJar().convention(
            this.getTaskNamed(DownloadMinecraftJarsTask.TASK_NAME, DownloadMinecraftJarsTask.class)
                .getServerBootstrapJar()
        );

        this.getServerJar().convention(() ->
            new File(this.fileConstants.cacheFilesMinecraft, Constants.MINECRAFT_VERSION + "-server.jar")
        );
    }

    @TaskAction
    public void extractServerJar() throws IOException {
        FileUtils.copyFile(
            // TODO eliminate project access in task action
            this.getProject()
                .zipTree(this.getServerBootstrapJar())
                .matching(patternFilterable -> patternFilterable.include("META-INF/versions/*/server-*.jar"))
                .getSingleFile(),
            this.getServerJar().get().getAsFile()
        );
    }
}
