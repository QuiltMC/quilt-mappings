package quilt.internal.extension;

import quilt.internal.extension.abstraction.DefaultTaskedExtension;
import quilt.internal.plugin.MapV2Plugin;

public abstract class MapV2Extension extends DefaultTaskedExtension<MapV2Plugin.Tasks> {
    public static final String NAME = "mapV2";

    public MapV2Extension(MapV2Plugin.Tasks tasks) {
        super(tasks);
    }
}
