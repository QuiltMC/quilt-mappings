package quilt.internal.plugin.abstraction;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Aids in a common pattern for implementing {@link ExtensionedMappingsProjectPlugin}.
 * <p>
 * Subclasses must implement {@link #applyImpl(Project)} instead of {@link Plugin#apply(Object)},
 * and implementations should return a newly {@linkplain ExtensionContainer#create(String, Class, Object...) created}
 * extension instance.
 *
 * @param <E> the type of the extension
 *            {@linkplain ExtensionContainer#create(String, Class, Object...) created} by this plugin
 */
public abstract class DefaultExtensionedMappingsProjectPlugin<E> implements ExtensionedMappingsProjectPlugin<E> {
    private E ext;

    /**
     * @throws NullPointerException if this plugin hasn't finished {@linkplain #apply(Project) applying}
     */
    public final E getExt() {
        return requireNonNullExt(this.ext);
    }

    @Override
    public final void apply(@NotNull Project project) {
        this.ext = this.applyImpl(project);
    }

    protected abstract E applyImpl(@NotNull Project project);

    public static <E> E requireNonNullExt(E ext) {
        return Objects.requireNonNull(ext, "Extension not yet registered");
    }
}
