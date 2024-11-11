package quilt.internal.task.setup;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.quiltmc.launchermeta.version.v1.DownloadableFile;
import org.quiltmc.launchermeta.version.v1.Downloads;
import quilt.internal.constants.Groups;
import quilt.internal.plugin.MinecraftJarsPlugin;
import quilt.internal.task.VersionParserConsumingTask;
import quilt.internal.util.DownloadUtil;

/**
 * Downloads the Minecraft client and server jars for the passed {@linkplain #getVersionParser version}.
 *
 * @see MinecraftJarsPlugin MinecraftJarsPlugin's configureEach
 */
public abstract class DownloadMinecraftJarsTask extends DefaultTask implements VersionParserConsumingTask {
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
        this.setGroup(Groups.SETUP);
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
}
