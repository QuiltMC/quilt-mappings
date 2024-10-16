package quilt.internal.tasks.unpick.gen;

import quilt.internal.tasks.MappingsTask;

/**
 * A task that outputs unpick files.
 * <p>
 * {@link quilt.internal.QuiltMappingsPlugin QuiltMappingsPlugin} adds the
 * {@link org.gradle.api.Task#getOutputs() outputs} of all
 * {@code UnpickGenTask}s to {@value quilt.internal.tasks.unpick.CombineUnpickDefinitionsTask#COMBINE_UNPICK_DEFINITIONS_TASK_NAME}'s
 * {@link quilt.internal.tasks.unpick.CombineUnpickDefinitionsTask#getUnpickDefinitions() unpickDefinitions},
 * so implementing tasks should <i>only</i> output unpick files.
 */
public interface UnpickGenTask extends MappingsTask { }
