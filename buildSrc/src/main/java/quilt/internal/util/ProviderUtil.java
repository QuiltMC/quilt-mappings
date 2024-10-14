package quilt.internal.util;

import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileSystemLocationProperty;
import org.gradle.api.provider.Provider;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public final class ProviderUtil {
    private ProviderUtil() { }

    public static  <T> Optional<T> toOptional(Provider<T> provider) {
        return Optional.ofNullable(provider.getOrNull());
    }

    public static Path getPath(FileSystemLocationProperty<? extends FileSystemLocation> location) {
        return location.get().getAsFile().toPath();
    }

    public static boolean exists(FileSystemLocationProperty<? extends FileSystemLocation> location) {
        return toOptional(location)
            .map(FileSystemLocation::getAsFile)
            .filter(File::exists)
            .isPresent();
    }
}
