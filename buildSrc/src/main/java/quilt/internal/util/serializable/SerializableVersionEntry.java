package quilt.internal.util.serializable;

import org.gradle.api.GradleException;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.launchermeta.version_manifest.VersionEntry;
import org.quiltmc.launchermeta.version_manifest.VersionManifest;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;

public final class SerializableVersionEntry extends VersionEntry implements Serializable {
    public static @Nullable SerializableVersionEntry of(File manifestFile, String version) {
        final VersionManifest manifest;
        try {
            manifest = manifestFile.exists()
                ? VersionManifest
                    .fromReader(Files.newBufferedReader(manifestFile.toPath(), Charset.defaultCharset()))
                : null;
        } catch (IOException e) {
            throw new GradleException("Failed to read manifest", e);
        }

        return manifest == null ? null :
            manifest.getVersions().stream()
                .filter(entry -> entry.getId().equals(version))
                .findFirst()
                .map(SerializableVersionEntry::new)
                .orElse(null);
    }

    public SerializableVersionEntry(VersionEntry entry) {
        super(entry.getId(), entry.getType(), entry.getUrl(), entry.getTime(), entry.getReleaseTime());
    }
}
