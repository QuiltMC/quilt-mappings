package quilt.internal.tasks.build;

import cuchaz.enigma.command.CheckMappingsCommand;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;

public class CheckMappingsTask extends MappingsTask {
    public CheckMappingsTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        getInputs().dir(fileConstants.mappingsDir);
        this.dependsOn("mapHashedMojmapJar");
    }

    @TaskAction
    public void checkMappings() {
        getLogger().lifecycle(":checking mappings");

        String[] args = new String[]{
                fileConstants.hashedMojmapJar.getAbsolutePath(),
                fileConstants.mappingsDir.getAbsolutePath()
        };

        try {
            new CheckMappingsCommand().run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
