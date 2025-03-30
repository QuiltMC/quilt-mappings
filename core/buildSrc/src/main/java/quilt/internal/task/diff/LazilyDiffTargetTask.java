package quilt.internal.task.diff;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import quilt.internal.constants.Groups;
import quilt.internal.plugin.TargetDiffPlugin;

public abstract class LazilyDiffTargetTask extends DefaultTask implements UnpickVersionsMatchConsumingTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link TargetDiffPlugin}.
     * <p>
     * A wrapper for {@value DiffTargetTask#DIFF_TARGET_TASK_NAME} that conditionally depends on
     * {@value DiffTargetTask#DIFF_TARGET_TASK_NAME} only if its
     * {@link TargetVersionConsumingTask#getTargetVersion() targetVersion} {@link Provider#isPresent() isPresent}.
     * <p>
     * This is a hack to prevent unnecessarily generating sources using local mappings when
     * {@link TargetVersionConsumingTask#getTargetVersion() targetVersion} isn't present.
     */
    public static final String LAZILY_DIFF_TARGET_TASK_NAME = "lazilyDiffTarget";

    public LazilyDiffTargetTask() {
        this.setGroup(Groups.DIFF);
    }
}
