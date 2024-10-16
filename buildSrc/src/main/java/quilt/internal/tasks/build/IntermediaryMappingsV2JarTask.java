package quilt.internal.tasks.build;

import quilt.internal.tasks.setup.IntermediaryDependantTask;

import javax.inject.Inject;

public abstract class IntermediaryMappingsV2JarTask extends MappingsV2JarTask implements IntermediaryDependantTask {
    public static final String INTERMEDIARY_V_2_MAPPINGS_JAR_TASK_NAME = "intermediaryV2MappingsJar";
    public static final String INTERMEDIARY_V_2_MERGED_MAPPINGS_JAR_TASK_NAME = "intermediaryV2MergedMappingsJar";

    @Inject
    public IntermediaryMappingsV2JarTask(String unpickVersion) {
        super(unpickVersion);
    }
}
