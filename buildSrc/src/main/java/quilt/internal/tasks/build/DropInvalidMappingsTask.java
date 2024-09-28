package quilt.internal.tasks.build;

import org.quiltmc.enigma.command.DropInvalidMappingsCommand;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.jarmapping.MapPerVersionMappingsJarTask;

import java.io.File;

public abstract class DropInvalidMappingsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "dropInvalidMappings";
    private final File mappings = this.getProject().file("mappings");

    public DropInvalidMappingsTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        this.getInputs().dir(this.mappings);
        this.dependsOn(MapPerVersionMappingsJarTask.TASK_NAME);
    }

    @TaskAction
    public void dropInvalidMappings() {
        this.getLogger().info(":dropping invalid mappings");

        final String[] args = new String[]{
            this.fileConstants.perVersionMappingsJar.getAbsolutePath(),
            this.mappings.getAbsolutePath()
        };

        try {
            new DropInvalidMappingsCommand().run(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
