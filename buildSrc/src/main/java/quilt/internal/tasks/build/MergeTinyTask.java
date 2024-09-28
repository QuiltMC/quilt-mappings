package quilt.internal.tasks.build;

import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

public abstract class MergeTinyTask extends AbstractHashedMergeTask {
    public static final String TASK_NAME = "mergeTiny";

    public MergeTinyTask() {
        super("mappings.tiny");
        this.dependsOn(InvertPerVersionMappingsTask.TASK_NAME, BuildMappingsTinyTask.TASK_NAME);

        this.getInput().convention(
            this.getTaskNamed(BuildMappingsTinyTask.TASK_NAME, BuildMappingsTinyTask.class).getOutputMappings()
        );
    }

    @VisibleForTesting
    public static void mergeMappings(
        Path buildMappingsTiny, Path invertedPerVersionsMappings, Path outputMappings
    ) throws IOException {
        AbstractTinyMergeTask.mergeMappings(buildMappingsTiny, invertedPerVersionsMappings, outputMappings,
            Function.identity(), Function.identity());
    }
}
