package quilt.internal.tasks;

import org.gradle.api.DefaultTask;
import quilt.internal.FileConstants;

// TODO possibly eliminate this
public abstract class DefaultMappingsTask extends DefaultTask implements MappingsTask {
    protected final FileConstants fileConstants;

    public DefaultMappingsTask(String group) {
        this.fileConstants = this.mappingsExt().getFileConstants();
        // TODO set groups in registerDefault methods instead of here
        this.setGroup(group);
    }
}
