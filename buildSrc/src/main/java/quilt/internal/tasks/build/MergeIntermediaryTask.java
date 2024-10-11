package quilt.internal.tasks.build;

import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.jetbrains.annotations.VisibleForTesting;
import quilt.internal.Constants;
import quilt.internal.mappingio.DoubleNsCompleterVisitor;
import quilt.internal.mappingio.UnmappedNameRemoverVisitor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class MergeIntermediaryTask extends AbstractTinyMergeTask {
    public static final String TASK_NAME = "mergeIntermediary";

    @InputFile
    public abstract RegularFileProperty getMergedTinyMappings();

    public MergeIntermediaryTask() {
        super(
            Constants.INTERMEDIARY_MAPPINGS_NAME,
            Constants.PER_VERSION_MAPPINGS_NAME
        );
    }

    @Override
    public void mergeMappings() throws Exception {
        this.mergeMappings(this.getMergedTinyMappings().get().getAsFile());
    }

    @Override
    protected MappingVisitor getFirstVisitor(MappingVisitor next) {
        return firstVisitor(next);
    }

    private static MappingVisitor firstVisitor(MappingVisitor next) {
        // Copy unobfuscated names to the named namespace, since intermediary would override them
        return new DoubleNsCompleterVisitor(
                // Fix bug when intermediary doesn't have a mapping but hashed does
                // (i.e. `net/minecraft/client/main/Main$2`)
                new DoubleNsCompleterVisitor(
                        new UnmappedNameRemoverVisitor(next, "named", Constants.PER_VERSION_MAPPINGS_NAME),
                        // Copy names from `official` to `named` if `intermediary` is empty
                        "named",
                        "intermediary",
                        "official"
                ),
                // Copy names from `official` to `named` if `hashed` is empty
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
    public static void mergeMappings(
        Path intermediaryMappings, Path mergeTinyV2Output, Path outputMappings
    ) throws IOException {
        AbstractTinyMergeTask.mergeMappings(intermediaryMappings, mergeTinyV2Output, outputMappings,
            MergeIntermediaryTask::firstVisitor,
            MergeIntermediaryTask::preWriteVisitor
        );
    }
}
