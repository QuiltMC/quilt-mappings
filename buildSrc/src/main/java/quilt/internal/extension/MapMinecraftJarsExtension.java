package quilt.internal.extension;

import quilt.internal.extension.abstraction.DefaultTaskedExtension;
import quilt.internal.plugin.MapMinecraftJarsPlugin;

public abstract class MapMinecraftJarsExtension extends DefaultTaskedExtension<MapMinecraftJarsPlugin.Tasks> {
    public static final String NAME = "mapMinecraftJars";

    public MapMinecraftJarsExtension(MapMinecraftJarsPlugin.Tasks tasks) {
        super(tasks);
    }
}
