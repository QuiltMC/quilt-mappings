package quilt.internal.tasks.setup;

import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.work.DisableCachingByDefault;
import quilt.internal.Constants.Groups;
import quilt.internal.plugin.MinecraftJarsPlugin;
import quilt.internal.tasks.SimpleDownloadTask;
import quilt.internal.util.SerializableVersionEntry;

@DisableCachingByDefault(because = "Output depends on a remote source that may change.")
public abstract class DownloadVersionsManifestTask extends SimpleDownloadTask {
    /**
     * {@linkplain org.gradle.api.tasks.TaskContainer#register Registered} by
     * {@link MinecraftJarsPlugin MinecraftJarsPlugin}.
     */
    public static final String DOWNLOAD_VERSIONS_MANIFEST_TASK_NAME = "downloadVersionsManifest";

    public Provider<SerializableVersionEntry> provideVersionEntry() {
        return this.getDest()
            .map(RegularFile::getAsFile)
            .map(SerializableVersionEntry::of);
    }

    public DownloadVersionsManifestTask() {
        super(Groups.SETUP);

        this.getPreDownloadLifecycle().convention(":downloading minecraft versions manifest");

        this.getUrl().convention("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");
    }
}
