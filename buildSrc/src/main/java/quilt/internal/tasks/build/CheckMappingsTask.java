package quilt.internal.tasks.build;

import java.io.File;

import cuchaz.enigma.command.CheckMappingsCommand;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.jarmapping.MapPerVersionMappingsJarTask;

public class CheckMappingsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "checkMappings";
    private final File mappings = getProject().file("mappings");

    public CheckMappingsTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        getInputs().dir(mappings);
        this.dependsOn(MapPerVersionMappingsJarTask.TASK_NAME);
    }

    @TaskAction
    public void checkMappings() {
        getLogger().lifecycle(":checking mappings");

        String[] args = new String[]{
                fileConstants.perVersionMappingsJar.getAbsolutePath(),
                mappings.getAbsolutePath()
        };

        try {
            new CheckMappingsCommand().run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
