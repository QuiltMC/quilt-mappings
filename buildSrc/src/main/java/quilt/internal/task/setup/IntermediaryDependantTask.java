package quilt.internal.task.setup;

import org.gradle.api.Task;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskCollection;
import quilt.internal.plugin.MapIntermediaryPlugin;
import quilt.internal.plugin.MapMinecraftJarsPlugin;
import quilt.internal.task.MappingsTask;

/**
 * A task that depends on {@value quilt.internal.Constants#INTERMEDIARY_MAPPINGS_NAME} mappings
 * for the current Minecraft version.
 *
 * @see MapIntermediaryPlugin MapIntermediaryPlugin's configureEach
 */
public interface IntermediaryDependantTask extends MappingsTask { }
