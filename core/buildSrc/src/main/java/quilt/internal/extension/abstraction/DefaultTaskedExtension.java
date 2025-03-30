package quilt.internal.extension.abstraction;

import org.gradle.api.plugins.ExtensionContainer;

/**
 * Aids in a common pattern for implementing {@link TaskedExtension}.
 * <p>
 * Plugins that create {@code DefaultTaskedExtension}s must pass their {@code tasks} instance as a
 * {@link ExtensionContainer#create(String, Class, Object...) construction argument} when creating their extension.
 *
 * @param <T> a type that provides access to some of the tasks registered by the plugin that creates this extension
 */
public abstract class DefaultTaskedExtension<T> implements TaskedExtension<T> {
    private final T tasks;

    @Override
    public final T getTasks() {
        return this.tasks;
    }

    public DefaultTaskedExtension(T tasks) {
        this.tasks = tasks;
    }
}
