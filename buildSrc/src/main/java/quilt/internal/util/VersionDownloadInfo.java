package quilt.internal.util;

import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;
import org.quiltmc.launchermeta.version.v1.DownloadableFile;
import org.quiltmc.launchermeta.version.v1.Downloads;
import org.quiltmc.launchermeta.version.v1.Version;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Holds download information from a {@link org.quiltmc.launchermeta.version.v1.Version Version}
 * so they can be cached and the {@link org.quiltmc.launchermeta.version.v1.Version Version}
 * needn't be parsed by multiple {@link org.gradle.api.Task Task}s.
 */
public class VersionDownloadInfo implements Serializable {
    public static VersionDownloadInfo of(File versionFile) {
        final Version version;
        try {
            version = Version.fromString(FileUtils.readFileToString(versionFile, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new GradleException("Failed to read version file", e);
        }

        final Downloads downloads = version.getDownloads();

        final var client = new SerializableDownloadableFile(downloads.getClient());

        final var server = new SerializableDownloadableFile(downloads.getServer().orElseThrow(() ->
            new GradleException("Version has no server download")
        ));

        final Map<String, String> libraryArtifactsUrlsByName = version.getLibraries().stream()
            .flatMap(library ->
                library.getDownloads().getArtifact()
                    .map(DownloadableFile.PathDownload::getUrl)
                    .map(artifact -> Map.entry(
                        library.getName(),
                        artifact
                    ))
                    .stream()
            )
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new VersionDownloadInfo(client, server, libraryArtifactsUrlsByName);
    }

    private final SerializableDownloadableFile client;

    private final SerializableDownloadableFile server;

    private final Map<String, String> libraryArtifactUrlsByName;

    public SerializableDownloadableFile getClient() {
        return this.client;
    }

    public SerializableDownloadableFile getServer() {
        return this.server;
    }

    public Map<String, String> getLibraryArtifactUrlsByName() {
        return this.libraryArtifactUrlsByName;
    }

    private VersionDownloadInfo(SerializableDownloadableFile client, SerializableDownloadableFile server, Map<String, String> libraryArtifactUrlsByName) {
        this.client = client;
        this.server = server;
        this.libraryArtifactUrlsByName = libraryArtifactUrlsByName;
    }
}
