package quilt.internal.util;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;

import java.io.File;
import java.nio.file.Path;

public final class ProviderUtil {
    private ProviderUtil() { }

    public static Path getPath(RegularFileProperty file) {
        return file.get().getAsFile().toPath();
    }

    public static Provider<Directory> provideProjectDir(Project project, File directory) {
        return project.getLayout().dir(project.provider(() -> directory));
    }
}
