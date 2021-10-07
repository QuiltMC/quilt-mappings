package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;

import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import net.fabricmc.stitch.merge.JarMerger;

public class MergeJarsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "mergeJars";

    private final DownloadMinecraftJarsTask downloadMinecraftJarsTask;

    @OutputFile
    private final File mergedFile;

    public MergeJarsTask() {
        super(Constants.Groups.SETUP_GROUP);
        dependsOn(DownloadMinecraftJarsTask.TASK_NAME);

        downloadMinecraftJarsTask = getTaskFromType(DownloadMinecraftJarsTask.class);
        getInputs().files(downloadMinecraftJarsTask.getClientJar(), downloadMinecraftJarsTask.getServerJar());

        mergedFile = getProject().file(Constants.MINECRAFT_VERSION + "-merged.jar");
        getOutputs().file(mergedFile);
    }

    @TaskAction
    public void mergeJars() throws IOException {
        getLogger().lifecycle(":merging jars");

        File client = downloadMinecraftJarsTask.getClientJar();
        File server = downloadMinecraftJarsTask.getServerJar();

        if (mergedFile.exists()) {
            return;
        }

        try (JarMerger jarMerger = new JarMerger(client, server, mergedFile)) {
            jarMerger.merge();
        }
    }

    public File getMergedFile() {
        return mergedFile;
    }
}
