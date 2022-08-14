package quilt.internal.tasks.build;

import quilt.internal.tasks.setup.DownloadIntermediaryMappingsTask;

public class InvertIntermediaryMappingsTask extends AbstractInvertMappingsTask {
    public static final String TASK_NAME = "invertIntermediaryMappings";

    public InvertIntermediaryMappingsTask() {
        super("intermediary");
        this.dependsOn(DownloadIntermediaryMappingsTask.TASK_NAME);

        input.convention(getTaskByType(DownloadIntermediaryMappingsTask.class)::getTinyFile);
    }
}
