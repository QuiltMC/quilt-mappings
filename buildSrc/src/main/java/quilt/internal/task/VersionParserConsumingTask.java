package quilt.internal.task;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import quilt.internal.plugin.MinecraftJarsPlugin;
import quilt.internal.util.serializable.VersionParser;

/**
 * A task that takes {@link VersionParser} as input.
 *
 * @see MinecraftJarsPlugin MinecraftJarsPlugin's configureEach
 */
public interface VersionParserConsumingTask extends MappingsTask {
    @Input
    Property<VersionParser> getVersionParser();
}
