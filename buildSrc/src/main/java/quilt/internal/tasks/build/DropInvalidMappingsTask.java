package quilt.internal.tasks.build;

import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.quiltmc.enigma.command.DropInvalidMappingsCommand;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.MappingsDirConsumingTask;

public abstract class DropInvalidMappingsTask extends DefaultMappingsTask implements MappingsDirConsumingTask {
    public static final String TASK_NAME = "dropInvalidMappings";

    @InputFile
    public abstract RegularFileProperty getPerVersionMappingsJar();

    public DropInvalidMappingsTask() {
        super(Constants.Groups.BUILD_MAPPINGS);
    }

    @TaskAction
    public void dropInvalidMappings() {
        this.getLogger().info(":dropping invalid mappings");

        final String[] args = new String[]{
            this.getPerVersionMappingsJar().get().getAsFile().getAbsolutePath(),
            this.getMappingsDir().get().getAsFile().getAbsolutePath()
        };

        try {
            new DropInvalidMappingsCommand().run(args);
        } catch (Exception e) {
            throw new GradleException("Failed to drop mappings", e);
        }
    }
}
