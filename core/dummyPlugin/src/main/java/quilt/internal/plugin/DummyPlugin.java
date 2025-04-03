package quilt.internal.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.plugin.management.PluginManagementSpec;
import org.jetbrains.annotations.NotNull;

/**
 * Dummy plugin for {@linkplain PluginManagementSpec#includeBuild(String) includeBuild}ing in the root project.
 * <p>
 * Its jar task depends on the task that publishes {@code gametestable-quilt-mappings} to a local repo.
 */
public abstract class DummyPlugin implements Plugin<Project> {
    @Override
    public void apply(@NotNull Project project) { }
}
