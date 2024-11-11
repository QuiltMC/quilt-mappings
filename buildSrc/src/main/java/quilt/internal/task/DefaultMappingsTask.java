package quilt.internal.task;

import org.gradle.api.DefaultTask;

public abstract class DefaultMappingsTask extends DefaultTask {
    public DefaultMappingsTask(String group) {
        this.setGroup(group);
    }
}
