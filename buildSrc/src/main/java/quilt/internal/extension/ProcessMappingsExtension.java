package quilt.internal.extension;

import quilt.internal.extension.abstraction.DefaultTaskedExtension;
import quilt.internal.plugin.ProcessMappingsPlugin;

public abstract class ProcessMappingsExtension extends DefaultTaskedExtension<ProcessMappingsPlugin.Tasks> {
    public static final String NAME = "processMappings";

    public ProcessMappingsExtension(ProcessMappingsPlugin.Tasks tasks) {
        super(tasks);
    }
}
