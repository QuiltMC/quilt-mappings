package quilt.internal.task.unpick.gen;

import quilt.internal.task.MappingsTask;

/**
 * A task that outputs unpick files.
 * <p>
 * {@link quilt.internal.plugin.QuiltMappingsPlugin QuiltMappingsPlugin} adds the
 * {@link org.gradle.api.Task#getOutputs() outputs} of all
 * {@code UnpickGenTask}s to {@value quilt.internal.task.unpick.CombineUnpickDefinitionsTask#COMBINE_UNPICK_DEFINITIONS_TASK_NAME}'s
 * {@link quilt.internal.task.unpick.CombineUnpickDefinitionsTask#getUnpickDefinitions() unpickDefinitions},
 * so implementing tasks should <i>only</i> output unpick files.
 */
public interface UnpickGenTask extends MappingsTask {
    String UNPICK_EXTENSION = "unpick";
}
