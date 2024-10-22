package quilt.internal.tasks.build;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import quilt.internal.Constants.Groups;

import java.io.IOException;

public abstract class AbstractHashedMergeTask extends AbstractTinyMergeTask {
    @InputFile
    public abstract RegularFileProperty getHashedTinyMappings();

    public AbstractHashedMergeTask() {
        super(Groups.PER_VERSION);
    }

    @Override
    public void mergeMappings() throws IOException {
        this.mergeMappings(this.getHashedTinyMappings().get().getAsFile());
    }
}
