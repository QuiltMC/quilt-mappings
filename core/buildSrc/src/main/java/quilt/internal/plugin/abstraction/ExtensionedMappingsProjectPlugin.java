package quilt.internal.plugin.abstraction;

import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginContainer;

/**
 * A plugin that {@linkplain ExtensionContainer#create(String, Class, Object...) creates} an extension.
 * <p>
 * Prefer retrieving extensions via {@link #getExt() ext} to {@link ExtensionContainer} {@code get/find...} methods.<br>
 * An instance of the plugin can be obtained via {@link PluginContainer#apply(Class)},
 * and accessing the extension via the returned plugin instance ensures that the extension has been created.
 *
 * @param <E> the type of the extension
 *            {@linkplain ExtensionContainer#create(String, Class, Object...) created} by this plugin
 *
 * @see DefaultExtensionedMappingsProjectPlugin
 */
public interface ExtensionedMappingsProjectPlugin<E> extends MappingsProjectPlugin {
    E getExt();
}
