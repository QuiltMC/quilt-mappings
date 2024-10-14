package quilt.internal.tasks;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;

public interface MappingsDirConsumingTask extends MappingsTask {
    @InputDirectory
    DirectoryProperty getMappingsDir();
}
