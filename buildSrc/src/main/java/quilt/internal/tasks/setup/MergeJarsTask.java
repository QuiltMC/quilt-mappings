package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import net.fabricmc.stitch.merge.JarMerger;

public class MergeJarsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "mergeJars";

    @InputFile
    private final RegularFileProperty clientJar;
    @InputFile
    private final RegularFileProperty serverJar;

    @OutputFile
    private final File mergedFile;

    public MergeJarsTask() {
        super(Constants.Groups.SETUP_GROUP);
        dependsOn(ExtractServerJarTask.TASK_NAME);

        clientJar = getProject().getObjects().fileProperty();
        serverJar = getProject().getObjects().fileProperty();

        clientJar.convention(getTaskByType(DownloadMinecraftJarsTask.class)::getClientJar);
        serverJar.convention(getTaskByType(ExtractServerJarTask.class)::getServerJar);

        getInputs().files(clientJar, serverJar);

        mergedFile = getProject().file(Constants.MINECRAFT_VERSION + "-merged.jar");
        getOutputs().file(mergedFile);
    }

    @TaskAction
    public void mergeJars() throws IOException {
        getLogger().lifecycle(":merging jars");

        if (mergedFile.exists()) {
            return;
        }

        try (JarMerger jarMerger = new JarMerger(clientJar.getAsFile().get(), serverJar.getAsFile().get(), mergedFile)) {
            jarMerger.merge();
        }
    }

    public RegularFileProperty getClientJar() {
        return clientJar;
    }

    public RegularFileProperty getServerJar() {
        return serverJar;
    }

    public File getMergedFile() {
        return mergedFile;
    }
}
