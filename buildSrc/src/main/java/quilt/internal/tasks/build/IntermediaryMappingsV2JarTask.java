package quilt.internal.tasks.build;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;

import javax.inject.Inject;

import static quilt.internal.util.ProviderUtil.exists;

public abstract class IntermediaryMappingsV2JarTask extends MappingsV2JarTask {
    @Override
    @Optional
    @InputFile
    public abstract RegularFileProperty getMappings();

    @Inject
    public IntermediaryMappingsV2JarTask(String unpickVersion) {
        super(unpickVersion);

        this.onlyIf(unused -> exists(this.getMappings()));
    }
}
