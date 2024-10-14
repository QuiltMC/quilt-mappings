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
 * will use {@value CheckUnpickVersionsMatchTask#TASK_NAME}'s
 * {@link CheckUnpickVersionsMatchTask#isMatch() match} by default, and they'll only run if
 * {@link #getUnpickVersionsMatch() unpickVersionsMatch} {@link Provider#isPresent() isPresent} and {@code true}.
 */
public interface UnpickVersionsMatchConsumingTask extends TargetVersionConsumingTask {
    @Input
    @Optional
    Property<Boolean> getUnpickVersionsMatch();
}
