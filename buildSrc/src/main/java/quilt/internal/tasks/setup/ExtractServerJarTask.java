package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public class ExtractServerJarTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "extractServerJar";

    @OutputFile
    private final File serverJar;

    public ExtractServerJarTask() {
        super(Constants.Groups.SETUP_GROUP);
        dependsOn(DownloadMinecraftJarsTask.TASK_NAME);

        getInputs().file(this.getTaskByType(DownloadMinecraftJarsTask.class).getServerBootstrapJar());

        serverJar = new File(fileConstants.cacheFilesMinecraft, Constants.MINECRAFT_VERSION + "-server.jar");
    }

    @TaskAction
    public void extractServerJar() throws IOException {
        FileUtils.copyFile(getProject().zipTree(this.getTaskByType(DownloadMinecraftJarsTask.class).getServerBootstrapJar()).matching(patternFilterable -> patternFilterable.include("META-INF/versions/*/server-*.jar")).getSingleFile(), serverJar);
    }

    public File getServerJar() {
        return serverJar;
    }
}
