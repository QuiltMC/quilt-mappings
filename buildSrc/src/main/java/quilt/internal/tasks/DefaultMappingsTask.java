package quilt.internal.tasks;

import org.gradle.api.DefaultTask;
import quilt.internal.FileConstants;
import quilt.internal.MappingsPlugin;

public abstract class DefaultMappingsTask extends DefaultTask implements AbstractMappingsTask {
    protected final FileConstants fileConstants;

    public DefaultMappingsTask(String group) {
        this.fileConstants = MappingsPlugin.getExtension(getProject()).getFileConstants();
        this.setGroup(group);
    }
}
