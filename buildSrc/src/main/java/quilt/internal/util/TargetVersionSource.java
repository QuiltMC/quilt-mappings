package quilt.internal.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.stream.StreamSupport;

import static quilt.internal.util.DownloadUtil.openAvailableConnection;

public abstract class TargetVersionSource implements ValueSource<String, TargetVersionSource.Params> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TargetVersionSource.class);

    @Override
    @Nullable
    public String obtain() {
        final String url = "https://meta.quiltmc.org/v3/versions/quilt-mappings/" +
            this.getParameters().getMinecraftVersion().get();

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
                        object -> object.get("build").getAsInt(),
                        Integer::compare
                    ))
                    .map(object -> object.get("version"))
                    .map(JsonElement::getAsString);
            })
            .orElseGet(() -> {
                LOGGER.warn(":target version meta unavailable");

                return null;
            });
    }

    public interface Params extends ValueSourceParameters {
        Property<String> getMinecraftVersion();
    }
}
