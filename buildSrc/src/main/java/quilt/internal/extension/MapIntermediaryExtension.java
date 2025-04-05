package quilt.internal.extension;

import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;

public abstract class MapIntermediaryExtension {
    public static final String NAME = "mapIntermediary";

    private final Provider<RegularFile> intermediaryFile;

    public Provider<RegularFile> getIntermediaryFile() {
        return this.intermediaryFile;
    }

    public MapIntermediaryExtension(Provider<RegularFile> intermediaryFile) {
        this.intermediaryFile = intermediaryFile;
    }
}
