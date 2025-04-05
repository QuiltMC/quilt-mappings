package quilt.internal.task.build;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.jvm.tasks.Jar;
import quilt.internal.constants.Extensions;
import quilt.internal.constants.Groups;
import quilt.internal.plugin.MapMinecraftJarsPlugin;
import quilt.internal.plugin.QuiltMappingsBasePlugin;
import quilt.internal.task.QuiltMappingsArtifactTask;

/**
 * Creates a jar file with the input {@link #getMappings() mappings} located at {@value JAR_MAPPINGS_PATH}.
 *
 * @see QuiltMappingsBasePlugin QuiltMappingsBasePlugin's configureEach
 */
public abstract class TinyJarTask extends Jar implements QuiltMappingsArtifactTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapMinecraftJarsPlugin}.
     */
    public static final String TINY_JAR_TASK_NAME = "tinyJar";

    public static final String JAR_MAPPINGS_PATH = "mappings/mappings." + Extensions.TINY;

    @InputFile
    public abstract RegularFileProperty getMappings();

    public TinyJarTask() {
        this.setGroup(Groups.BUILD_MAPPINGS);

        this.from(this.getMappings()).rename(original -> JAR_MAPPINGS_PATH);
    }
}
