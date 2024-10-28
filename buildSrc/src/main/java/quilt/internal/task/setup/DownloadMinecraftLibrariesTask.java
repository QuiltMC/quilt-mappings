package quilt.internal.task.setup;

import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;
import org.quiltmc.launchermeta.version.v1.DownloadableFile;
import org.quiltmc.launchermeta.version.v1.Version;
import quilt.internal.Constants.Groups;
import quilt.internal.plugin.MapMinecraftJarsPlugin;
import quilt.internal.plugin.MinecraftJarsPlugin;
import quilt.internal.task.DefaultMappingsTask;
import quilt.internal.task.VersionParserConsumingTask;
import quilt.internal.util.DownloadUtil;
import quilt.internal.util.serializable.VersionParser;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Downloads the Minecraft's libraries for the passed {@linkplain #getVersionParser version}.
 *
 * @see MinecraftJarsPlugin MinecraftJarsPlugin's configureEach
 */
// TODO why?
@DisableCachingByDefault(because = "unknown")
public abstract class DownloadMinecraftLibrariesTask extends DefaultMappingsTask implements
    VersionParserConsumingTask {
    /**
     * {@linkplain org.gradle.api.tasks.TaskContainer#register Registered} by
     * {@link MinecraftJarsPlugin MinecraftJarsPlugin}.
     */
    public static final String DOWNLOAD_MINECRAFT_LIBRARIES_TASK_NAME = "downloadMinecraftLibraries";

    /**
     * @see MapMinecraftJarsPlugin
     */
    @OutputDirectory
    public abstract DirectoryProperty getLibrariesDir();

    @OutputFiles
    abstract MapProperty<NamedUrl, RegularFile> getArtifactsByNamedUrl();

    @OutputFiles
    abstract MapProperty<String, File> getArtifactsByNameImpl();

    @OutputFiles
    public Provider<Map<String, File>> getArtifactsByName() {
        return this.getArtifactsByNameImpl();
    }

    @Inject
    protected abstract ObjectFactory getObjects();

    public DownloadMinecraftLibrariesTask() {
        super(Groups.SETUP);

        // put this in a property to cache it
        final Provider<Map<NamedUrl, RegularFile>> artifactsByNamedUrl =
            this.getObjects().mapProperty(NamedUrl.class, RegularFile.class).convention(
                this.getVersionParser()
                    .map(VersionParser::get)
                    .map(version -> getArtifactsByNamedUrl(version, this.getLibrariesDir().get()))
            );

        this.getArtifactsByNamedUrl().convention(artifactsByNamedUrl);

        // don't map from getArtifactsByNamedUrl() because that
        // would access a task output before execution has completed
        this.getArtifactsByNameImpl().set(
            artifactsByNamedUrl
                .map(urlDestsByName ->
                    urlDestsByName.entrySet().stream()
                        .collect(Collectors.toMap(entry -> entry.getKey().name(), entry -> entry.getValue().getAsFile()))
                )
        );
    }

    @TaskAction
    public void download() {
        this.getArtifactsByNamedUrl().get().entrySet().parallelStream().forEach(entry ->
            DownloadUtil.download(entry.getKey().url, entry.getValue().getAsFile(), false, this.getLogger())
        );
    }

    private static Map<NamedUrl, RegularFile> getArtifactsByNamedUrl(Version version, Directory destDir) {
        return version.getLibraries().stream()
            .flatMap(library ->
                library.getDownloads().getArtifact()
                    .map(DownloadableFile.PathDownload::getUrl)
                    .map(artifact -> Map.entry(
                        library.getName(),
                        artifact
                    ))
                    .stream()
            )
            .collect(Collectors.toMap(
                entry -> new NamedUrl(entry.getKey(), entry.getValue()),
                entry -> artifactOf(entry.getValue(), destDir)
            ));
    }

    private static RegularFile artifactOf(String url, Directory dest) {
        return dest.file(url.substring(url.lastIndexOf("/") + 1));
    }

    private record NamedUrl(String name, String url) implements Serializable { }
}
