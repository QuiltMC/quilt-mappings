package quilt.internal.tasks.build;

public class MergeTinyTask extends AbstractHashedMergeTask {
    public static final String TASK_NAME = "mergeTiny";

    public MergeTinyTask() {
        super("mappings.tiny");
        dependsOn(InvertPerVersionMappingsTask.TASK_NAME, BuildMappingsTinyTask.TASK_NAME);

        input.convention(getTaskByType(BuildMappingsTinyTask.class)::getOutputMappings);
    }
}
