package quilt.internal.tasks;

import org.gradle.api.DefaultTask;
import quilt.internal.FileConstants;

public abstract class DefaultMappingsTask extends DefaultTask implements MappingsTask {
    protected final FileConstants fileConstants;

    public DefaultMappingsTask(String group) {
        this.fileConstants = mappingsExt().getFileConstants();
        this.setGroup(group);
    }
}
