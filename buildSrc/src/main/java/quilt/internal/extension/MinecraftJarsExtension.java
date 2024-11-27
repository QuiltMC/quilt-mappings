package quilt.internal.extension;

import quilt.internal.extension.abstraction.DefaultTaskedExtension;
import quilt.internal.plugin.MinecraftJarsPlugin;

public abstract class MinecraftJarsExtension extends DefaultTaskedExtension<MinecraftJarsPlugin.Tasks> {
    public static final String NAME = "minecraftJars";

    public MinecraftJarsExtension(MinecraftJarsPlugin.Tasks tasks) {
        super(tasks);
    }
}
