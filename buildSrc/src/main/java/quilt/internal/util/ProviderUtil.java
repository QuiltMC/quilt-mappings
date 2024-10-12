package quilt.internal.util;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileSystemLocationProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public final class ProviderUtil {
    private ProviderUtil() { }

    public static Path getPath(FileSystemLocationProperty<?> location) {
        return location.get().getAsFile().toPath();
    }

    public static  <T> Optional<T> toOptional(Provider<T> provider) {
        return Optional.ofNullable(provider.getOrNull());
    }

    public static Provider<Directory> provideProjectDir(Project project, File directory) {
        return project.getLayout().dir(project.provider(() -> directory));
    }
}
