package quilt.internal.tasks.diff;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.FileReader;

import static quilt.internal.tasks.diff.UnpickVersionsMatchSource.*;
import static quilt.internal.util.ProviderUtil.toOptional;

/**
 * @see UnpickVersionsMatchConsumingTask
 */
public abstract class UnpickVersionsMatchSource implements ValueSource<Boolean, Params> {
    @Override
    public Boolean obtain() {
        final var params = this.getParameters();

        return toOptional(params.getUnpickVersion())
            .filter(version -> {
                final JsonElement parsed;
                try {
                    parsed = JsonParser.parseReader(new FileReader(params.getUnpickMeta().getAsFile().get()));
                } catch (FileNotFoundException e) {
                    throw new GradleException("Failed to read unpick meta", e);
                }

                return parsed.getAsJsonObject().get("unpickVersion").getAsString().equals(version);
            })
            .isPresent();
    }

    public interface Params extends ValueSourceParameters {
        RegularFileProperty getUnpickMeta();

        Property<String> getUnpickVersion();
    }
}
