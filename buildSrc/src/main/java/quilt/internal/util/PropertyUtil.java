package quilt.internal.util;

import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;

import java.io.File;
import java.nio.file.Path;

public final class PropertyUtil {
    private PropertyUtil() { }

    public static Path getPath(RegularFileProperty fileProperty) {
        return fileProperty.map(RegularFile::getAsFile).map(File::toPath).get();
    }
}
