package quilt.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import quilt.internal.FileConstants;
import quilt.internal.MappingsPlugin;
import quilt.internal.util.DownloadImmediate;

public abstract class DefaultMappingsTask extends DefaultTask implements MappingsTask {
    protected final FileConstants fileConstants;

    public DefaultMappingsTask(String group) {
        this.fileConstants = MappingsPlugin.getExtension(getProject()).getFileConstants();
        this.setGroup(group);
    }
}
