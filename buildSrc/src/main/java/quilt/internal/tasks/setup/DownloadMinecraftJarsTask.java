package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.quiltmc.launchermeta.version.v1.Downloads;
import org.quiltmc.launchermeta.version.v1.Version;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public abstract class DownloadMinecraftJarsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadMinecraftJars";

    @InputFile
    public abstract RegularFileProperty getVersionFile();

    @Internal("Fingerprinting is handled by getVersionFile")
    protected abstract Property<Version> getVersion();

    @OutputFile
    public abstract RegularFileProperty getClientJar();

    @OutputFile
    public abstract RegularFileProperty getServerBootstrapJar();

    public DownloadMinecraftJarsTask() {
        super(Constants.Groups.SETUP_GROUP);

        this.getVersion().convention(this.getVersionFile().map(file -> {
            try {
                return Version.fromReader(java.nio.file.Files.newBufferedReader(
                    file.getAsFile().toPath(),
                    StandardCharsets.UTF_8
                ));
            } catch (IOException e) {
                throw new GradleException("Failed to read version file", e);
            }
        }));

        this.getOutputs().upToDateWhen(unused -> {
            try {
                final File clientJar = this.getClientJar().get().getAsFile();
                final File serverBootstrapJar = this.getServerBootstrapJar().get().getAsFile();
                final Downloads versionDownloads = this.getVersion().get().getDownloads();

                return clientJar.exists() && serverBootstrapJar.exists()
                    && validateChecksum(clientJar, versionDownloads.getClient().getSha1())
                    && validateChecksum(serverBootstrapJar, versionDownloads.getServer().orElseThrow().getSha1());
            } catch (Exception e) {
                return false;
            }
        });
    }

    @TaskAction
    public void downloadMinecraftJars() throws IOException {
        this.getLogger().lifecycle(":downloading minecraft jars");

        final Downloads versionDownloads = this.getVersion().get().getDownloads();

        this.startDownload()
                .src(versionDownloads.getClient().getUrl())
                .dest(this.getClientJar().get().getAsFile())
                .overwrite(false)
                .download();

        this.startDownload()
                .src(versionDownloads.getServer().orElseThrow().getUrl())
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
}
