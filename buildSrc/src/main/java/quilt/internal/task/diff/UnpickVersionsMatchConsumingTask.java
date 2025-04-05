package quilt.internal.task.diff;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import quilt.internal.plugin.QuiltMappingsPlugin;
import quilt.internal.plugin.TargetDiffPlugin;

/**
 * A task that takes whether two unpick versions match as input.
 *
 * @see TargetDiffPlugin TargetDiffPlugin's configureEach[es]
 */
public interface UnpickVersionsMatchConsumingTask extends TargetVersionConsumingTask {
    @Input
    Property<Boolean> getUnpickVersionsMatch();
}
