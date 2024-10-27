package quilt.internal.task;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import quilt.internal.plugin.QuiltMappingsBasePlugin;

/**
 * A task that takes a directory containing mappings as input.
 *
 * @see QuiltMappingsBasePlugin QuiltMappingsBasePlugin's configureEach
 */
public interface MappingsDirConsumingTask extends MappingsTask {
    @InputDirectory
    DirectoryProperty getMappingsDir();
}
