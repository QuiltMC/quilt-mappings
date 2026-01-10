package quilt.internal.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.plugin.management.PluginManagementSpec;
import org.jetbrains.annotations.NotNull;

/**
 * Dummy plugin for {@linkplain PluginManagementSpec#includeBuild(String) includeBuild}ing in the root project,
 * and for exposing the {@code :core} project's version to the {@code :gametest} project.
 * <p>
 * Its jar task depends on the task that publishes {@code gametestable-quilt-mappings} to a local repo.<br>
 * This ensures gametestable mappings are always available for the gametest project.
 */
public abstract class GametestablePlugin implements Plugin<Project> {
    @Override
    public void apply(@NotNull Project project) {
        project.getExtensions().create(
            "gametestable",
            GametestableExtension.class,
            GametestablePlugin.class.getPackage().getImplementationVersion()
        );
    }
}
