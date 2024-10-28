package quilt.internal.task.mappings;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.UntrackedTask;
import quilt.internal.Constants.Groups;
import quilt.internal.plugin.QuiltMappingsBasePlugin;
import quilt.internal.task.EnigmaProfileConsumingTask;
import quilt.internal.task.MappingsDirConsumingTask;

import java.util.List;

/**
 * @see QuiltMappingsBasePlugin QuiltMappingsBasePlugin's configureEach
 */
@UntrackedTask(because =
    """
    These input and output to the same directory, which doesn't work with Gradle's task graph.
    These tasks' outputs should not be consumed by other tasks.
    """
)
public abstract class AbstractEnigmaMappingsTask extends JavaExec implements
        EnigmaProfileConsumingTask, MappingsDirConsumingTask {
    @InputFile
    public abstract RegularFileProperty getJarToMap();

    public AbstractEnigmaMappingsTask() {
        this.setGroup(Groups.MAPPINGS);

        this.getArgumentProviders().add(() -> List.of(
            "-jar", this.getJarToMap().get().getAsFile().getAbsolutePath(),
            "-mappings", this.getMappingsDir().get().getAsFile().getAbsolutePath(),
            "-profile", this.getEnigmaProfileConfig().get().getAsFile().getAbsolutePath()
        ));
    }
}
