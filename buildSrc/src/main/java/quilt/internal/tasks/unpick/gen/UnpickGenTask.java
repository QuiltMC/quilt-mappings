package quilt.internal.tasks.unpick.gen;

import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.OutputFiles;

/**
 * A task that outputs unpick files.
 * <p>
 * If {@link quilt.internal.QuiltMappingsPlugin QuiltMappingsPlugin} is applied, the {@link Task#getOutputs() outputs}
 * of all {@code UnpickGenTask}s will be passed as input to
 * {@value quilt.internal.tasks.unpick.CombineUnpickDefinitionsTask#TASK_NAME},
 * so implementing tasks should <i>only</i> output unpick files.
 */
public interface UnpickGenTask extends Task { }
