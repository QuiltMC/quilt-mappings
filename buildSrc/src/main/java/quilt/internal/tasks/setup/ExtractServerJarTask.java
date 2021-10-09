package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

        getInputs().file(this.getTaskFromType(DownloadMinecraftJarsTask.class).getServerBootstrapJar());

        serverJar = new File(fileConstants.cacheFilesMinecraft, Constants.MINECRAFT_VERSION + "-server.jar");
    }

    @TaskAction
    public void extractServerJar() throws IOException {
        try (ZipFile bootstrap = new ZipFile(this.getTaskFromType(DownloadMinecraftJarsTask.class).getServerBootstrapJar())) {

            ZipEntry serverEntry = bootstrap.getEntry("META-INF/versions/" + Constants.MINECRAFT_VERSION + "/server-" + Constants.MINECRAFT_VERSION + ".jar");

            if (serverEntry == null) {
                throw new RuntimeException("Unable to find server jar entry in " + bootstrap.getName() + " for version " + Constants.MINECRAFT_VERSION);
            }

            try (InputStream stream = bootstrap.getInputStream(serverEntry)) {
                Files.write(serverJar.toPath(), stream.readAllBytes());
            }
        }
    }

    public File getServerJar() {
        return serverJar;
    }
}
