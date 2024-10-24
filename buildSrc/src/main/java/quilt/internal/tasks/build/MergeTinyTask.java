package quilt.internal.tasks.build;

import org.gradle.api.tasks.TaskContainer;
import org.jetbrains.annotations.VisibleForTesting;
import quilt.internal.plugin.MapMinecraftJarsPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

public abstract class MergeTinyTask extends AbstractHashedMergeTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapMinecraftJarsPlugin}.
     */
    public static final String MERGE_TINY_TASK_NAME = "mergeTiny";

    @VisibleForTesting
    public static void mergeMappings(
        Path buildMappingsTiny, Path invertedPerVersionsMappings, Path outputMappings
    ) throws IOException {
        AbstractTinyMergeTask.mergeMappings(buildMappingsTiny, invertedPerVersionsMappings, outputMappings,
            Function.identity(), Function.identity());
    }
}
