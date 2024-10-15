package quilt.internal.tasks;

import org.gradle.api.DefaultTask;

// TODO eliminate this
public abstract class DefaultMappingsTask extends DefaultTask implements MappingsTask {
    public DefaultMappingsTask(String group) {
        this.setGroup(group);
    }
}
