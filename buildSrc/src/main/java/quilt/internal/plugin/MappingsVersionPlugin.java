package quilt.internal.plugin;

import org.gradle.api.Project;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;
import quilt.internal.extension.QuiltMappingsExtension;
import quilt.internal.plugin.abstraction.MappingsProjectPlugin;
import quilt.internal.util.ProviderUtil;
import quilt.internal.util.Version;

public abstract class MappingsVersionPlugin implements MappingsProjectPlugin {
    @Override
    public void apply(@NotNull Project project) {
        final PluginContainer plugins = project.getPlugins();

        final QuiltMappingsExtension quiltExt = plugins.apply(QuiltMappingsBasePlugin.class).getExt();

        final String minecraftVersion = quiltExt.getMinecraftVersion();

        final Property<Version> targetVersionProperty = quiltExt.getTargetVersion();
        targetVersionProperty.finalizeValue();
        final int buildNumber = ProviderUtil.toOptional(targetVersionProperty)
            .map(Version::build)
            .map(targetBuild -> targetBuild + 1)
            .orElse(1);

        final String mappingVersion = minecraftVersion + "+build." + buildNumber;

        quiltExt.getMappingsVersion().set(mappingVersion);

        project.setVersion(mappingVersion);
    }
}
