package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.quiltmc.launchermeta.version.v1.Version;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public abstract class DownloadMinecraftJarsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadMinecraftJars";

    @OutputFile
    public abstract RegularFileProperty getClientJar();

    @OutputFile
    public abstract RegularFileProperty getServerBootstrapJar();

    private Version file;

    public DownloadMinecraftJarsTask() {
        super(Constants.Groups.SETUP_GROUP);
        this.dependsOn(DownloadWantedVersionManifestTask.TASK_NAME);

        this.getClientJar().convention(() -> new File(
            this.fileConstants.cacheFilesMinecraft,
            Constants.MINECRAFT_VERSION + "-client.jar"
        ));
        this.getServerBootstrapJar().convention(() -> new File(
                this.fileConstants.cacheFilesMinecraft,
                Constants.MINECRAFT_VERSION + "-server-bootstrap.jar"
        ));

        this.getOutputs().upToDateWhen(_input -> {
            try {
                return this.getClientJar().get().getAsFile().exists()
                    && this.getServerBootstrapJar().get().getAsFile().exists()
                    && validateChecksum(
                        this.getClientJar().get().getAsFile(),
                        this.getVersionFile().getDownloads().getClient().getSha1()
                    )
                    && validateChecksum(
                        this.getServerBootstrapJar().get().getAsFile(),
                        this.getVersionFile().getDownloads().getServer().orElseThrow().getSha1()
                    );
            } catch (Exception e) {
                return false;
            }
        });
        this.finalizedBy(ExtractServerJarTask.TASK_NAME);
    }

    @TaskAction
    public void downloadMinecraftJars() throws IOException {
        this.getLogger().lifecycle(":downloading minecraft jars");

        this.startDownload()
                .src(this.getVersionFile().getDownloads().getClient().getUrl())
                .dest(this.getClientJar().get().getAsFile())
                .overwrite(false)
                .download();

        this.startDownload()
                .src(this.getVersionFile().getDownloads().getServer().orElseThrow().getUrl())
                .dest(this.getServerBootstrapJar().get().getAsFile())
                .overwrite(false)
                .download();
    }

    @SuppressWarnings("deprecation")
    private static boolean validateChecksum(File file, String checksum) throws IOException {
        if (file != null) {
            final HashCode hash = Files.asByteSource(file).hash(Hashing.sha1());
            final StringBuilder builder = new StringBuilder();
            for (final byte b : hash.asBytes()) {
                builder.append(Integer.toString((b & 0xFF) + 0x100, 16).substring(1));
            }

            return builder.toString().equals(checksum);
        }

        return false;
    }

    private Version getVersionFile() throws IOException {
        if (this.file == null) {
            this.file = Version.fromReader(java.nio.file.Files.newBufferedReader(
                // TODO eliminate project access in task action
                this.getTaskNamed(DownloadWantedVersionManifestTask.TASK_NAME, DownloadWantedVersionManifestTask.class)
                    .getVersionFile().get().getAsFile().toPath(),
                StandardCharsets.UTF_8
            ));
        }

        return this.file;
    }
}
