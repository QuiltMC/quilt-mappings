package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.quiltmc.launchermeta.version.v1.Version;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public class DownloadMinecraftJarsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadMinecraftJars";
    @OutputFile
    private final File clientJar;

    @OutputFile
    private final File serverBootstrapJar;

    private Version file;

    public DownloadMinecraftJarsTask() {
        super(Constants.Groups.SETUP_GROUP);
        this.dependsOn(DownloadWantedVersionManifestTask.TASK_NAME);

        clientJar = new File(fileConstants.cacheFilesMinecraft, Constants.MINECRAFT_VERSION + "-client.jar");
        serverBootstrapJar = new File(fileConstants.cacheFilesMinecraft, Constants.MINECRAFT_VERSION + "-server-bootstrap.jar");
        getOutputs().files(clientJar, serverBootstrapJar);

        getOutputs().upToDateWhen(_input -> {
            try {
                return clientJar.exists() && serverBootstrapJar.exists()
                       && validateChecksum(clientJar, getVersionFile().getDownloads().getClient().getSha1())
                       && validateChecksum(serverBootstrapJar, getVersionFile().getDownloads().getServer().get().getSha1());
            } catch (Exception e) {
                return false;
            }
        });
    }

    @TaskAction
    public void downloadMinecraftJars() throws IOException {
        getLogger().lifecycle(":downloading minecraft jars");

        startDownload()
                .src(getVersionFile().getDownloads().getClient().getUrl())
                .dest(clientJar)
                .overwrite(false)
                .download();

        startDownload()
                .src(getVersionFile().getDownloads().getServer().get().getUrl())
                .dest(serverBootstrapJar)
                .overwrite(false)
                .download();
    }

    @SuppressWarnings("deprecation")
    private static boolean validateChecksum(File file, String checksum) throws IOException {
        if (file != null) {
            HashCode hash = Files.asByteSource(file).hash(Hashing.sha1());
            StringBuilder builder = new StringBuilder();
            for (byte b : hash.asBytes()) {
                builder.append(Integer.toString((b & 0xFF) + 0x100, 16).substring(1));
            }
            return builder.toString().equals(checksum);
        }
        return false;
    }

    private Version getVersionFile() throws IOException {
        if (file == null) {
            file = Version.fromReader(java.nio.file.Files.newBufferedReader(getTaskByType(DownloadWantedVersionManifestTask.class).getVersionFile().toPath(), StandardCharsets.UTF_8));
        }

        return file;
    }

    public File getClientJar() {
        return clientJar;
    }

    public File getServerBootstrapJar() {
        return serverBootstrapJar;
    }
}
