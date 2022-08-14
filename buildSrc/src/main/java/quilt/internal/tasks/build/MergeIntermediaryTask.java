package quilt.internal.tasks.build;

import java.io.File;

public class MergeIntermediaryTask extends AbstractTinyMergeTask {
    public static final String TASK_NAME = "mergeIntermediary";

    public MergeIntermediaryTask() {
        super("mergedIntermediary.tiny", "intermediary");
        dependsOn(InvertIntermediaryMappingsTask.TASK_NAME, MergeTinyV2Task.TASK_NAME);

        input.convention(getTaskByType(MergeTinyV2Task.class)::getOutputMappings);
    }

    @Override
    public void mergeMappings() throws Exception {
        File intermediaryTinyInput = this.getTaskByType(InvertIntermediaryMappingsTask.class).getInvertedTinyFile();
        mergeMappings(intermediaryTinyInput);
    }
}
