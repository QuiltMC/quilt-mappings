package quilt.internal.task.build;

import org.gradle.api.tasks.TaskContainer;
import quilt.internal.constants.Constants;
import quilt.internal.plugin.MapIntermediaryPlugin;
import quilt.internal.plugin.MapV2Plugin;
import quilt.internal.plugin.QuiltMappingsBasePlugin;
import quilt.internal.task.setup.IntermediaryDependantTask;

import javax.inject.Inject;

/**
 * Creates a jar file containing {@value Constants#INTERMEDIARY_MAPPINGS_NAME} mappings in Quilt's v2 mapping format.
 *
 * @see QuiltMappingsBasePlugin QuiltMappingsBasePlugin's configureEach
 * @see MapIntermediaryPlugin MapIntermediaryPlugin's configureEach
 * @see MapV2Plugin MapV2Plugin's configureEach
 */
public abstract class IntermediaryMappingsV2JarTask extends MappingsV2JarTask implements IntermediaryDependantTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapIntermediaryPlugin}.
     */
    public static final String INTERMEDIARY_V_2_MAPPINGS_JAR_TASK_NAME = "intermediaryV2MappingsJar";
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapIntermediaryPlugin}.
     */
    public static final String INTERMEDIARY_V_2_MERGED_MAPPINGS_JAR_TASK_NAME = "intermediaryV2MergedMappingsJar";

    @Inject
    public IntermediaryMappingsV2JarTask(String unpickVersion) {
        super(unpickVersion);
    }
}
