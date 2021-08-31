package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;

import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.FileConstants;
import quilt.internal.MappingsPlugin;
import quilt.internal.tasks.MappingsTask;

import net.fabricmc.stitch.merge.JarMerger;

public class MergeJarsTask extends MappingsTask {
    public MergeJarsTask() {
        super(Constants.Groups.SETUP_GROUP);
        dependsOn("downloadMinecraftJars");
        getInputs().files(getTaskFromName("downloadMinecraftJars").getOutputs().getFiles().getFiles());
        FileConstants fileConstants = MappingsPlugin.getExtension(getProject()).getFileConstants();
        getOutputs().file(fileConstants.mergedFile);
    }

    @TaskAction
    public void mergeJars() throws IOException {
        getLogger().lifecycle(":merging jars");

        File client = getInputs().getFiles().getFiles().stream().filter(file -> file.getName().endsWith("-client.jar")).findFirst().get();
        File server = getInputs().getFiles().getFiles().stream().filter(file -> file.getName().endsWith("-server.jar")).findFirst().get();

        if (fileConstants.mergedFile.exists()) {
            return;
        }

        try(JarMerger jarMerger = new JarMerger(client, server, fileConstants.mergedFile)) {
            jarMerger.merge();
        }
    }
}
