package quilt.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import quilt.internal.FileConstants;
import quilt.internal.MappingsPlugin;
import quilt.internal.tasks.setup.DownloadVersionsManifestTask;
import quilt.internal.util.DownloadImmediate;

public abstract class MappingsTask extends DefaultTask {
    protected final FileConstants fileConstants;

    public MappingsTask(String group) {
        this.fileConstants = MappingsPlugin.getExtension(getProject()).getFileConstants();
        this.setGroup(group);
    }

    public DownloadImmediate.Builder startDownload() {
        return new DownloadImmediate.Builder(this);
    }

    @SuppressWarnings("unchecked")
    protected <T extends Task> T getTaskFromName(String taskName) {
        return (T) getProject().getTasks().getByName(taskName);
    }
}
