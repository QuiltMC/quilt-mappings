package quilt.internal.extension.abstraction;

import org.gradle.api.Plugin;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskContainer;
import quilt.internal.plugin.abstraction.ExtensionedMappingsProjectPlugin;

/**
 * An extension that provides access to some of the {@link Task}s {@linkplain TaskContainer#register registered}
 * by the {@link Plugin plugin} that {@linkplain ExtensionContainer#create creates} it.
 * <p>
 * Prefer retrieving {@link Task}s via {@link #getTasks()} to {@link TaskContainer#named} methods.
 *
 * @param <T> a type that provides access to some of the tasks registered by the plugin that creates this extension
 *
 * @see DefaultTaskedExtension
 * @see ExtensionedMappingsProjectPlugin
 */
public interface TaskedExtension<T> {
    T getTasks();
}
