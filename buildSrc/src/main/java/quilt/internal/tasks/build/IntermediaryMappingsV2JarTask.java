package quilt.internal.tasks.build;

import javax.inject.Inject;

import static quilt.internal.util.ProviderUtil.exists;

public abstract class IntermediaryMappingsV2JarTask extends MappingsV2JarTask {
    @Inject
    public IntermediaryMappingsV2JarTask(String unpickVersion) {
        super(unpickVersion);

        this.onlyIf(unused -> exists(this.getMappings()));
    }
}
