package quilt.internal.tasks.diff;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import quilt.internal.QuiltMappingsPlugin;

/**
 * A task that takes whether two unpick versions match as input.
 * <p>
 * If {@link QuiltMappingsPlugin MappingsPlugin} is applied, any {@code UnpickVersionsMatchConsumingTask}s
 * will use an {@link UnpickVersionsMatchSource} by default, and they'll only run if their
 * {@link #getUnpickVersionsMatch() unpickVersionsMatch} is {@code true}.
 */
public interface UnpickVersionsMatchConsumingTask extends TargetVersionConsumingTask {
    @Input
    @Optional
    Property<Boolean> getUnpickVersionsMatch();
}
