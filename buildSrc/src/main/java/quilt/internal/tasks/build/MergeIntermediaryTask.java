package quilt.internal.tasks.build;

import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.adapter.MappingNsCompleter;
import org.jetbrains.annotations.VisibleForTesting;
import quilt.internal.Constants;
import quilt.internal.tasks.setup.CheckIntermediaryMappingsTask;
import quilt.internal.tasks.setup.DownloadIntermediaryMappingsTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MergeIntermediaryTask extends AbstractTinyMergeTask {
    public static final String TASK_NAME = "mergeIntermediary";

    public MergeIntermediaryTask() {
        super("mappings-intermediaryMerged.tiny", "intermediary", Constants.PER_VERSION_MAPPINGS_NAME);
        dependsOn(CheckIntermediaryMappingsTask.TASK_NAME, DownloadIntermediaryMappingsTask.TASK_NAME, MergeTinyV2Task.TASK_NAME);
        onlyIf(task -> getTaskByType(CheckIntermediaryMappingsTask.class).isPresent());

        input.convention(getTaskByType(DownloadIntermediaryMappingsTask.class)::getTinyFile);
    }

    @Override
    public void mergeMappings() throws Exception {
        File tinyInput = this.getTaskByType(MergeTinyV2Task.class).getOutputMappings();
        mergeMappings(tinyInput);
    }

    @Override
    protected MappingVisitor getFirstVisitor(MappingVisitor next) {
        return firstVisitor(next);
    }

    private static MappingVisitor firstVisitor(MappingVisitor next) {
        // Fill missing hashed names with their corresponding unobfuscated official name
        // Ran first for unnamed classes to also be filled with those names
        return new MappingNsCompleter(next, Collections.singletonMap(Constants.PER_VERSION_MAPPINGS_NAME, "official"));
    }

    @Override
    protected MappingVisitor getPreWriteVisitor(MappingVisitor writer) {
        return preWriteVisitor(writer);
    }

    private static MappingVisitor preWriteVisitor(MappingVisitor writer) {
        return new MappingDstNsReorder(writer, List.of("intermediary", "named")); // Remove hashed namespace
    }

    @VisibleForTesting
    public static void mergeMappings(Path intermediaryMappings, Path mergeTinyV2Output, Path outputMappings) throws IOException {
        AbstractTinyMergeTask.mergeMappings(intermediaryMappings, mergeTinyV2Output, outputMappings,
            MergeIntermediaryTask::firstVisitor,
            MergeIntermediaryTask::preWriteVisitor,
            Map.of("named", Constants.PER_VERSION_MAPPINGS_NAME));
    }
}
