package quilt.internal.task.build;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskContainer;
import org.quiltmc.enigma.command.DropInvalidMappingsCommand;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.constants.Groups;
import quilt.internal.plugin.MapMinecraftJarsPlugin;
import quilt.internal.plugin.QuiltMappingsBasePlugin;
import quilt.internal.task.MappingsDirConsumingTask;

import java.nio.file.Path;

/**
 * Removes any invalid mappings found in the passed {@link #getMappingsDir() mappingsDir}.
 * <p>
 * Invalid mappings are usually the result of differences between Minecraft versions.
 *
 * @see QuiltMappingsBasePlugin QuiltMappingsBasePlugin's configureEach
 */
public abstract class DropInvalidMappingsTask extends DefaultTask implements MappingsDirConsumingTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapMinecraftJarsPlugin}.
     */
    public static final String DROP_INVALID_MAPPINGS_TASK_NAME = "dropInvalidMappings";

    @InputFile
    public abstract RegularFileProperty getPerVersionMappingsJar();

    public DropInvalidMappingsTask() {
        this.setGroup(Groups.BUILD_MAPPINGS);
    }

    @TaskAction
    public void dropInvalidMappings() {
        this.getLogger().info(":dropping invalid mappings");

        try {
            final Path jar = this.getPerVersionMappingsJar().get().getAsFile().toPath().toAbsolutePath();
            final Path mappings = this.getMappingsDir().get().getAsFile().toPath().toAbsolutePath();
            DropInvalidMappingsCommand.run(jar, mappings, mappings);
        } catch (Exception e) {
            throw new GradleException("Failed to drop mappings", e);
        }
    }
}
