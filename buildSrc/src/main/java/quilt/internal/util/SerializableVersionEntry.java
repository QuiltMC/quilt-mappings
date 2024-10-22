package quilt.internal.util;

import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.launchermeta.version_manifest.VersionEntry;
import org.quiltmc.launchermeta.version_manifest.VersionManifest;
import quilt.internal.Constants;
import quilt.internal.tasks.setup.DownloadWantedVersionManifestTask;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;

public final class SerializableVersionEntry extends VersionEntry implements Serializable {
    public static @Nullable SerializableVersionEntry of(File manifestFile) {
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
                .filter(entry -> entry.getId().equals(Constants.MINECRAFT_VERSION))
                .findFirst()
                .map(SerializableVersionEntry::new)
                .orElse(null);
    }

    public SerializableVersionEntry(VersionEntry entry) {
        super(entry.getId(), entry.getType(), entry.getUrl(), entry.getTime(), entry.getReleaseTime());
    }

    public static abstract class Source implements ValueSource<SerializableVersionEntry, Source.Params> {
        @Override
        @Nullable
        public SerializableVersionEntry obtain() {
            final File manifestFile = this.getParameters().getManifestFile().get().getAsFile();

            return of(manifestFile);
        }

        public interface Params extends ValueSourceParameters {
            RegularFileProperty getManifestFile();
        }
    }
}
