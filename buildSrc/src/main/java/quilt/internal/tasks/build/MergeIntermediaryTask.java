package quilt.internal.tasks.build;

import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import org.jetbrains.annotations.VisibleForTesting;
import quilt.internal.Constants;
import quilt.internal.mappingio.DoubleNsCompleterVisitor;
import quilt.internal.mappingio.UnmappedNameRemoverVisitor;
import quilt.internal.tasks.setup.CheckIntermediaryMappingsTask;
import quilt.internal.tasks.setup.DownloadIntermediaryMappingsTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
        // Copy unobfuscated names to the named namespace, since intermediary would override them
        return new DoubleNsCompleterVisitor(
                // Fix bug when intermediary doesn't have a mapping but hashed does (i.e. `net/minecraft/client/main/Main$2`)
                new DoubleNsCompleterVisitor(
                        new UnmappedNameRemoverVisitor(next, "named", Constants.PER_VERSION_MAPPINGS_NAME),
                        "named",
                        "intermediary",
                        "official"
                ),
                "named",
                Constants.PER_VERSION_MAPPINGS_NAME,
                "official"
        );
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
            MergeIntermediaryTask::preWriteVisitor
        );
    }
}
