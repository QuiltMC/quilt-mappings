package quilt.internal.tasks.setup;

public abstract class DownloadIntermediaryMappingsTask extends DownloadMappingsTask {
    public static final String TASK_NAME = "downloadIntermediaryMappings";

    public DownloadIntermediaryMappingsTask() {
        this.dependsOn(CheckIntermediaryMappingsTask.TASK_NAME);
        this.onlyIf(task ->
            this.getTaskNamed(CheckIntermediaryMappingsTask.TASK_NAME, CheckIntermediaryMappingsTask.class).isPresent()
        );
    }
}
