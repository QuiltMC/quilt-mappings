package quilt.internal.tasks.build;

import java.io.File;

public class MergeTinyTask extends AbstractTinyMergeTask {
    public static final String TASK_NAME = "mergeTiny";

    public MergeTinyTask() {
        super("mappings.tiny");
        dependsOn(InvertPerVersionMappingsTask.TASK_NAME, BuildMappingsTinyTask.TASK_NAME);
    }

    @Override
    public File getInputTinyFile() {
        return this.getTaskByType(BuildMappingsTinyTask.class).getOutputMappings();
    }
}
