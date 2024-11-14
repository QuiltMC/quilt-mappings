package quilt.internal.task.setup;

import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import quilt.internal.constants.Groups;
import quilt.internal.plugin.MinecraftJarsPlugin;
import quilt.internal.task.SimpleDownloadTask;
import quilt.internal.task.VersionParserConsumingTask;
import quilt.internal.util.serializable.SerializableVersionEntry;
import quilt.internal.util.serializable.VersionParser;

public abstract class DownloadWantedVersionManifestTask extends SimpleDownloadTask {
    /**
     * {@linkplain org.gradle.api.tasks.TaskContainer#register Registered} by
     * {@link MinecraftJarsPlugin MinecraftJarsPlugin}.
     */
    public static final String DOWNLOAD_WANTED_VERSION_MANIFEST_TASK_NAME = "downloadWantedVersionManifest";

    @Input
    public abstract Property<SerializableVersionEntry> getManifestVersion();

    @Override
    @OutputFile
    public abstract RegularFileProperty getDest();

    @Internal("Depends on output")
    abstract Property<VersionParser> getVersionParserImpl();

    /**
     * @see VersionParserConsumingTask
     * @see MinecraftJarsPlugin
     */
    public Provider<VersionParser> provideVersionParser() {
        return this.getVersionParserImpl();
    }

    public DownloadWantedVersionManifestTask() {
        this.setGroup(Groups.SETUP);

        this.getVersionParserImpl().set(
            this.getDest()
                .map(RegularFile::getAsFile)
                .map(VersionParser::new)
        );

        this.getUrl().convention(this.getManifestVersion().map(SerializableVersionEntry::getUrl));
    }
}
