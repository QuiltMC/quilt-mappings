package quilt.internal.util.serializable;

import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.quiltmc.launchermeta.version_manifest.VersionEntry;
import org.quiltmc.launchermeta.version_manifest.VersionManifest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;

import static quilt.internal.util.DownloadUtil.urlOf;

public final class SerializableVersionEntry extends VersionEntry implements Serializable {
    public SerializableVersionEntry(VersionEntry entry) {
        super(entry.getId(), entry.getType(), entry.getUrl(), entry.getTime(), entry.getReleaseTime());
    }

    public abstract static class Source implements ValueSource<SerializableVersionEntry, Source.Params> {
        @Override
        public SerializableVersionEntry obtain() {
            final URL url = urlOf(this.getParameters().getUrl().get());

            final VersionManifest manifest;
            try (var in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                manifest = VersionManifest.fromReader(in);
            } catch (IOException e) {
                throw new GradleException("Failed to read manifest from url: " + url, e);
            }

            return manifest.getVersions().stream()
                .filter(entry -> entry.getId().equals(this.getParameters().getVersion().get()))
                .findFirst()
                .map(SerializableVersionEntry::new)
                .orElseThrow(() -> new GradleException("No matching version in manifest"));
        }

        public interface Params extends ValueSourceParameters {
            Property<String> getUrl();

            Property<String> getVersion();
        }
    }
}
