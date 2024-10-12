package quilt.internal.tasks.mappings;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.Internal;
import quilt.internal.tasks.MappingsTask;

/**
 * A task that outputs mappings to the {@linkplain #getMappingsDir() mappings directory}.
 * <p>
 * All tasks that output to the mappings directory should implement this interface so
 * {@link quilt.internal.QuiltMappingsPlugin QuiltMappingsPlugin} can add their outputs to the inputs of
 * {@link quilt.internal.tasks.MappingsDirConsumingTask MappingsDirConsumingTask}s.
 * <p>
 * An implementing task should <i>only</i> output to files within {@link #getMappingsDir() mappingsDir} and should
 * <b>not</b> output to the whole directory unless it is an {@link org.gradle.api.tasks.UntrackedTask @UntrackedTask}
 * whose output is not intended for consumption by other tasks.
 */
public interface MappingsDirOutputtingTask extends MappingsTask {
    @Internal(
        """
        This is only used to resolve relative output paths against.
        A task should not add the whole directory to its output unless the task is
        untracked and its output is not intended for consumption by other tasks.
        """
    )
    DirectoryProperty getMappingsDir();
}
