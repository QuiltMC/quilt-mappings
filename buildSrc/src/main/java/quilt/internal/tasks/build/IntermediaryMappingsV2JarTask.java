package quilt.internal.tasks.build;

import quilt.internal.Constants.Namespaces;
import quilt.internal.tasks.setup.IntermediaryDependantTask;

import javax.inject.Inject;

public abstract class IntermediaryMappingsV2JarTask extends MappingsV2JarTask implements IntermediaryDependantTask {
    public static final String INTERMEDIARY_V_2_MAPPINGS_JAR_TASK_NAME = "intermediaryV2MappingsJar";
    public static final String INTERMEDIARY_V_2_MERGED_MAPPINGS_JAR_TASK_NAME = "intermediaryV2MergedMappingsJar";

    public static final String CLASSIFIER = Namespaces.INTERMEDIARY + "-v2";
    public static final String MERGED_CLASSIFIER = Namespaces.INTERMEDIARY + "-mergedv2";

    @Inject
    public IntermediaryMappingsV2JarTask(String unpickVersion) {
        super(unpickVersion);
    }
}
