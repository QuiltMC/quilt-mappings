package quilt.internal.util;

import org.gradle.api.file.RegularFileProperty;

import java.nio.file.Path;

public final class PropertyUtil {
    private PropertyUtil() { }

    public static Path getPath(RegularFileProperty fileProperty) {
        return fileProperty.get().getAsFile().toPath();
    }
}
