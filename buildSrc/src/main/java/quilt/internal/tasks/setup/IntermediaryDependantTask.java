package quilt.internal.tasks.setup;

import org.gradle.api.tasks.TaskCollection;
import quilt.internal.plugin.MapIntermediaryPlugin;
import quilt.internal.tasks.MappingsTask;

/**
 * A task that depends on {@value quilt.internal.Constants#INTERMEDIARY_MAPPINGS_NAME} mappings
 * for the current Minecraft version.
 * <p>
 * {@link MapIntermediaryPlugin} {@linkplain TaskCollection#configureEach configures} some defaults.
 */
public interface IntermediaryDependantTask extends MappingsTask { }
