package quilt.internal.task;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import quilt.internal.plugin.QuiltMappingsBasePlugin;

/**
 * A task that creates an artifact whose name uses Quilt Mappings' name and version.
 * <p>
 * Has no effect if the implementing task isn't a subclass of either
 * {@link ArtifactFileTask} or {@link AbstractArchiveTask}.
 * <p>
 * {@link QuiltMappingsBasePlugin} {@linkplain TaskCollection#configureEach(Action) configures}
 * the base name and the version.
 */
public interface QuiltMappingsArtifactTask extends MappingsTask {
    static boolean isInstance(Task task) {
        return task instanceof QuiltMappingsArtifactTask;
    }
}
