package quilt.internal.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Comparator;
import java.util.stream.StreamSupport;

import static quilt.internal.util.DownloadUtil.openAvailableConnection;

public record Version(String string, int build) implements Serializable {
    public abstract static class TargetSource implements ValueSource<Version, TargetSource.Params> {
        private static final String BUILD_KEY = "build";
        private static final String VERSION_KEY = "version";

        private static final String QUILT_MAPPINGS_META_URL = "https://meta.quiltmc.org/v3/versions/quilt-mappings";

        @Override
        @Nullable
        public Version obtain() {
            if (openAvailableConnection(QUILT_MAPPINGS_META_URL).isEmpty()) {
                throw new GradleException("Quilt mappings meta unavailable!");
            }

            final String url = QUILT_MAPPINGS_META_URL + "/" + this.getParameters().getMinecraftVersion().get();

            return openAvailableConnection(url)
                .flatMap(connection -> {
                    final JsonElement meta;
                    try (var in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        meta = JsonParser.parseReader(in);
                    } catch (IOException e) {
                        throw new GradleException("Failed to read meta file from url: " + url, e);
                    }

                    return StreamSupport.stream(meta.getAsJsonArray().spliterator(), false)
                        .map(JsonElement::getAsJsonObject)
                        .max(Comparator.comparing(
                            json -> json.get(BUILD_KEY).getAsInt(),
                            Integer::compare
                        ))
                        .map(json -> new Version(
                            json.get(VERSION_KEY).getAsString(),
                            json.get(BUILD_KEY).getAsInt()
                        ));
                })
                .orElse(null);
        }

        public interface Params extends ValueSourceParameters {
            Property<String> getMinecraftVersion();
        }
    }
}
