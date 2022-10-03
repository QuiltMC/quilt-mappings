package quilt.internal.tasks.setup;

public class DownloadIntermediaryMappingsTask extends AbstractDownloadMappingsTask {
    public static final String TASK_NAME = "downloadIntermediaryMappings";

    public DownloadIntermediaryMappingsTask() {
        super("intermediary");
        dependsOn(CheckIntermediaryMappingsTask.TASK_NAME);
        onlyIf(task -> getTaskByType(CheckIntermediaryMappingsTask.class).isPresent());
    }
}
