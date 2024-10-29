package quilt.internal.task.setup;

import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
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

    @OutputFile
    public abstract RegularFileProperty getDest();

    /**
     * @see VersionParserConsumingTask
     * @see MinecraftJarsPlugin
     */
    public Provider<VersionParser> provideVersionParser() {
        return this.getDest()
                .map(RegularFile::getAsFile)
                .map(VersionParser::new);
    }

    public DownloadWantedVersionManifestTask() {
        super(Groups.SETUP);

        // have to grab the release time as there's a current timestamp on each element?!
        // TODO I don't think this is necessary, the fact that manifestVersion is an input should take care of it.
        //  If it's not necessary, we could eliminate SerializableVersionEntry and parse the VersionEntry in the
        //  task action like before
        //  (and not extend SimpleDownloadTask since we'd take the manifest file as input instead of a url).
        this.getInputs().property(
            "releaseTime",
            this.getManifestVersion().map(SerializableVersionEntry::getReleaseTime)
        );

        this.getUrl().convention(this.getManifestVersion().map(SerializableVersionEntry::getUrl));
    }
}
