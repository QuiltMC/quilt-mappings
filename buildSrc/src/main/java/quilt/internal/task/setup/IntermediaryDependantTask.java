package quilt.internal.task.setup;

import quilt.internal.constants.Constants;
import quilt.internal.plugin.MapIntermediaryPlugin;
import quilt.internal.task.MappingsTask;

/**
 * A task that depends on {@value Constants#INTERMEDIARY_MAPPINGS_NAME} mappings
 * for the current Minecraft version.
 *
 * @see MapIntermediaryPlugin MapIntermediaryPlugin's configureEach
 */
public interface IntermediaryDependantTask extends MappingsTask { }
