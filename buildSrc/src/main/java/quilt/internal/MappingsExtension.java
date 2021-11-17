package quilt.internal;

import org.gradle.api.Project;

public class MappingsExtension {
    private final FileConstants fileConstants;

    public MappingsExtension(Project target) {
        fileConstants = new FileConstants(target);
    }

    public FileConstants getFileConstants() {
        return fileConstants;
    }
}
