package quilt.internal.plugin.abstraction;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Aids in a common pattern for implementing {@link TaskedMappingsProjectPlugin}.
 * <p>
 * Subclasses must implement {@link #applyImpl(Project)} instead of {@link Plugin#apply(Object)}
 * and implementations should return an object that provides access to some of the tasks registered by the plugin.
 *
 * @param <T> a type that provides access to some of the tasks registered by this plugin
 */
public abstract class DefaultTaskedMappingsProjectPlugin<T> implements TaskedMappingsProjectPlugin<T> {
    @Nullable
    private T tasks;

    /**
     * @throws NullPointerException if this plugin hasn't finished {@linkplain #apply(Project) applying}
     */
    @Override
    public final T getTasks() {
        return requireNonNullTasks(this.tasks);
    }

    @Override
    public final void apply(@NotNull Project project) {
        this.tasks = this.applyImpl(project);
    }

    protected abstract T applyImpl(@NotNull Project project);

    public static <T> T requireNonNullTasks(T tasks) {
        return Objects.requireNonNull(tasks, "Tasks not yet registered");
    }
}
