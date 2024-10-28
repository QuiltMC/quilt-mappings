package quilt.internal.plugin;

import org.gradle.api.Project;
import org.gradle.api.plugins.PluginContainer;
import org.jetbrains.annotations.NotNull;
import quilt.internal.plugin.abstraction.MappingsProjectPlugin;

/**
 * Main Quilt Mappings plugin.
 * <p>
 * Aggregate plugin that applies all other {@link MappingsProjectPlugin}s.
 */
public abstract class QuiltMappingsPlugin implements MappingsProjectPlugin {
    @Override
    public void apply(@NotNull Project project) {
        final PluginContainer plugins = project.getPlugins();

        plugins.apply(QuiltMappingsBasePlugin.class);
        plugins.apply(MinecraftJarsPlugin.class);
        plugins.apply(MapMinecraftJarsPlugin.class);
        plugins.apply(MapV2Plugin.class);
        plugins.apply(MapIntermediaryPlugin.class);
        plugins.apply(ProcessMappingsPlugin.class);
        plugins.apply(EnigmaMappingsPlugin.class);
        plugins.apply(MappingsVerificationPlugin.class);
        plugins.apply(TargetDiffPlugin.class);
    }
}
