package quilt.internal.task.setup;

import java.io.File;
import java.io.IOException;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.quiltmc.launchermeta.version.v1.DownloadableFile;
import org.quiltmc.launchermeta.version.v1.Downloads;
import quilt.internal.Constants.Groups;
import quilt.internal.plugin.MinecraftJarsPlugin;
import quilt.internal.task.DefaultMappingsTask;
import quilt.internal.task.VersionParserConsumingTask;
import quilt.internal.util.DownloadUtil;

/**
 * Downloads the Minecraft client and server jars for the passed {@linkplain #getVersionParser version}.
 *
 * @see MinecraftJarsPlugin MinecraftJarsPlugin's configureEach
 */
public abstract class DownloadMinecraftJarsTask extends DefaultMappingsTask implements VersionParserConsumingTask {
    /**
     * {@linkplain org.gradle.api.tasks.TaskContainer#register Registered} by
     * {@link MinecraftJarsPlugin MinecraftJarsPlugin}.
     */
    public static final String DOWNLOAD_MINECRAFT_JARS_TASK_NAME = "downloadMinecraftJars";

    @OutputFile
    public abstract RegularFileProperty getClientJar();

    @OutputFile
    public abstract RegularFileProperty getServerBootstrapJar();

    public DownloadMinecraftJarsTask() {
        super(Groups.SETUP);

        // TODO I'm not sure that this is necessary
        //  VersionParserConsumingTasks indirectly depend on
        //  DownloadVersionManifestFileTask which has @DisableCachingByDefault.
        //  I'm not sure if gradle considers output files to have been updated when they're overwritten with
        //  the same content.
        //  If not, this can be eliminated.
        //  If so, we should do a check like this in DownloadVersionManifestFileTask so checks like this are obsolete.
        this.getOutputs().upToDateWhen(unused -> {
            try {
                final File clientJar = this.getClientJar().get().getAsFile();
                final File serverBootstrapJar = this.getServerBootstrapJar().get().getAsFile();
                final Downloads downloads = this.getVersionParser().get().get().getDownloads();

                return clientJar.exists() && serverBootstrapJar.exists()
                    && validateChecksum(clientJar, downloads.getClient().getSha1())
                    && validateChecksum(serverBootstrapJar, getServerOrThrow(downloads).getSha1());
            } catch (Exception e) {
                return false;
            }
        });
    }

    @TaskAction
    public void download() {
        this.getLogger().lifecycle(":downloading minecraft jars");

        final Downloads downloads = this.getVersionParser().get().get().getDownloads();

        DownloadUtil.download(
            downloads.getClient().getUrl(),
            this.getClientJar().get().getAsFile(),
            false,
            this.getLogger()
        );

        DownloadUtil.download(
            getServerOrThrow(downloads).getUrl(),
            this.getServerBootstrapJar().get().getAsFile(),
            false,
            this.getLogger()
        );
    }

    private static DownloadableFile getServerOrThrow(Downloads downloads) {
        return downloads.getServer()
            .orElseThrow(() -> new GradleException("Version has no server download"));
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
