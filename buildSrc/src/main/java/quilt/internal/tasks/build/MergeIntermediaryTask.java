package quilt.internal.tasks.build;

import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;

import java.io.File;
import java.util.List;

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

    @Override
    protected MappingVisitor getPreWriteVisitor(MappingVisitor writer) {
        return new MappingDstNsReorder(writer, List.of("intermediary", "named")); // Remove hashed namespace
    }
}
