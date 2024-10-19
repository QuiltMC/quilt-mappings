package quilt.internal.tasks;

import org.gradle.api.DefaultTask;

public abstract class DefaultMappingsTask extends DefaultTask implements MappingsTask {
    // TODO eliminate this, eliminate subclasses that just specify group
    public DefaultMappingsTask(String group) {
        this.setGroup(group);
    }
}
