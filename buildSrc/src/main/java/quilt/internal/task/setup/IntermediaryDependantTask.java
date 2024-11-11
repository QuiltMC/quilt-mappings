package quilt.internal.task.setup;

import org.gradle.api.Task;
import quilt.internal.constants.Namespaces;
import quilt.internal.plugin.MapIntermediaryPlugin;

/**
 * A task that depends on {@value Namespaces#INTERMEDIARY} mappings for the current Minecraft version.
 *
 * @see MapIntermediaryPlugin MapIntermediaryPlugin's configureEach
 */
public interface IntermediaryDependantTask extends Task { }
