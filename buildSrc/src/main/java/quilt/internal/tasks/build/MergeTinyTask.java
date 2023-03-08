package quilt.internal.tasks.build;

import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

public class MergeTinyTask extends AbstractHashedMergeTask {
    public static final String TASK_NAME = "mergeTiny";

    public MergeTinyTask() {
        super("mappings.tiny");
        dependsOn(InvertPerVersionMappingsTask.TASK_NAME, BuildMappingsTinyTask.TASK_NAME);

        input.convention(getTaskByType(BuildMappingsTinyTask.class)::getOutputMappings);
    }

    @VisibleForTesting
    public static void mergeMappings(Path buildMappingsTiny, Path invertedPerVersionsMappings, Path outputMappings) throws IOException {
        AbstractTinyMergeTask.mergeMappings(buildMappingsTiny, invertedPerVersionsMappings, outputMappings,
            Function.identity(), Function.identity());
    }
}
