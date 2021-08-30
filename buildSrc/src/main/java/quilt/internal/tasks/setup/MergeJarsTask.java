package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;

import org.gradle.api.DefaultTask;
import quilt.internal.Constants;
import quilt.internal.FileConstants;
import quilt.internal.MappingsPlugin;

import net.fabricmc.stitch.merge.JarMerger;

public class MergeJarsTask extends DefaultTask {
    public MergeJarsTask() {
        dependsOn("downloadMinecraftJars");
        setGroup(Constants.Groups.SETUP_GROUP);
        getInputs().files(getProject().getTasks().getByName("downloadMinecraftJars").getOutputs().getFiles().getFiles());
        FileConstants fileConstants = MappingsPlugin.getExtension(getProject()).getFileConstants();
        getOutputs().file(fileConstants.mergedFile);

        doLast(_this -> {
            getLogger().lifecycle(":merging jars");

            File client = getInputs().getFiles().getFiles().stream().filter(file -> file.getName().endsWith("-client.jar")).findFirst().get();
            File server = getInputs().getFiles().getFiles().stream().filter(file -> file.getName().endsWith("-server.jar")).findFirst().get();

            if (fileConstants.mergedFile.exists()) {
                return;
            }

            try(JarMerger jarMerger = new JarMerger(client, server, fileConstants.mergedFile)) {
                jarMerger.merge();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
