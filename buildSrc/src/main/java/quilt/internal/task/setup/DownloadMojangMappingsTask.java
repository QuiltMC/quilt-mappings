package quilt.internal.task.setup;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.quiltmc.launchermeta.version.v1.Downloads;
import quilt.internal.constants.Groups;
import quilt.internal.plugin.MinecraftJarsPlugin;
import quilt.internal.task.VersionParserConsumingTask;
import quilt.internal.util.DownloadUtil;

/**
 * Downloads Mojang's client and server mappings for the passed {@linkplain #getVersionParser version}.
 *
 * @see <a href=https://minecraft.wiki/w/Obfuscation_map>Obfuscation map</a>
 * @see MinecraftJarsPlugin MinecraftJarsPlugin's configureEach
 */
public abstract class DownloadMojangMappingsTask extends DefaultTask implements VersionParserConsumingTask {
    /**
     * {@linkplain org.gradle.api.tasks.TaskContainer#register Registered} by
     * {@link MinecraftJarsPlugin MinecraftJarsPlugin}.
     */
    public static final String DOWNLOAD_MOJANG_MAPPINGS_TASK_NAME = "downloadMojangMappings";

    @OutputFile
    public abstract RegularFileProperty getClientMappings();

    @OutputFile
    public abstract RegularFileProperty getServerMappings();

    public DownloadMojangMappingsTask() {
        this.setGroup(Groups.SETUP);
    }

    @TaskAction
    public void download() {
        final Downloads downloads = this.getVersionParser().get().get().getDownloads();

        downloads.getClientMappings().ifPresentOrElse(
            clientMappings -> DownloadUtil.download(
                clientMappings.getUrl(),
                this.getClientMappings().get().getAsFile(),
                false, this.getLogger()
            ),
            () -> this.getLogger().warn("No client mappings available")
        );

        downloads.getServerMappings().ifPresentOrElse(
            serverMappings -> DownloadUtil.download(
                serverMappings.getUrl(),
                this.getServerMappings().get().getAsFile(),
                false, this.getLogger()
            ),
            () -> this.getLogger().warn("No server mappings available")
        );
    }
}
