package quilt.internal.tasks.build;

import quilt.internal.tasks.setup.CheckIntermediaryMappingsTask;
import quilt.internal.tasks.setup.DownloadIntermediaryMappingsTask;

public class InvertIntermediaryMappingsTask extends AbstractInvertMappingsTask {
    public static final String TASK_NAME = "invertIntermediaryMappings";

    public InvertIntermediaryMappingsTask() {
        super("intermediary");
        dependsOn(CheckIntermediaryMappingsTask.TASK_NAME, DownloadIntermediaryMappingsTask.TASK_NAME);
        onlyIf(task -> getTaskByType(CheckIntermediaryMappingsTask.class).isPresent());

        input.convention(getTaskByType(DownloadIntermediaryMappingsTask.class)::getTinyFile);
    }
}
