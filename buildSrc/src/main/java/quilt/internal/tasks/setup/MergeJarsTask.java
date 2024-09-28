package quilt.internal.tasks.setup;

import java.io.IOException;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import net.fabricmc.stitch.merge.JarMerger;

public abstract class MergeJarsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "mergeJars";

    @InputFile
    public abstract RegularFileProperty getClientJar();

    @InputFile
    public abstract RegularFileProperty getServerJar();

    @OutputFile
    public abstract RegularFileProperty getMergedFile();

    public MergeJarsTask() {
        super(Constants.Groups.SETUP_GROUP);
        this.dependsOn(ExtractServerJarTask.TASK_NAME);

        this.getClientJar().convention(
            this.getTaskNamed(DownloadMinecraftJarsTask.TASK_NAME, DownloadMinecraftJarsTask.class).getClientJar()
        );
        this.getServerJar().convention(
            this.getTaskNamed(ExtractServerJarTask.TASK_NAME, ExtractServerJarTask.class).getServerJar()
        );

        this.getMergedFile().convention(
            this.createRegularProjectFile(Constants.MINECRAFT_VERSION + "-merged.jar")
        );
    }

    @TaskAction
    public void mergeJars() throws IOException {
        this.getLogger().lifecycle(":merging jars");

        if (this.getMergedFile().get().getAsFile().exists()) {
            return;
        }

        try (JarMerger jarMerger = new JarMerger(
            this.getClientJar().get().getAsFile(),
            this.getServerJar().get().getAsFile(),
            this.getMergedFile().get().getAsFile()
        )) {
            jarMerger.merge();
        }
    }
}
