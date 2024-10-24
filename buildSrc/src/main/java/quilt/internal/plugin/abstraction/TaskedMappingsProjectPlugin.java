package quilt.internal.plugin.abstraction;

import org.gradle.api.Task;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.TaskContainer;

/**
 * A plugin that {@link TaskContainer#register registers} one or more {@link Task}s
 * intended for use by other plugins.
 * <p>
 * Prefer retrieving {@link Task}s via {@link #getTasks()} to {@link TaskContainer#named} methods.<br>
 * An instance of the plugin can be obtained via {@link PluginContainer#apply(Class)},
 * and accessing tasks via the returned plugin instance ensures that the tasks have been registered.
 *
 * @param <T> a type that provides access to some of the tasks registered by this plugin
 */
public interface TaskedMappingsProjectPlugin<T> extends MappingsProjectPlugin {
    T getTasks();
}
