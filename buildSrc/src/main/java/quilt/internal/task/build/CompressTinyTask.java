package quilt.internal.task.build;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import quilt.internal.Constants.Groups;
import quilt.internal.plugin.MapMinecraftJarsPlugin;
import quilt.internal.plugin.QuiltMappingsBasePlugin;
import quilt.internal.task.ArtifactFileTask;
import quilt.internal.task.DefaultMappingsTask;
import quilt.internal.task.QuiltMappingsArtifactTask;

/**
 * Compresses the input {@link #getMappings() mappings} using GZip.
 *
 * @see QuiltMappingsBasePlugin QuiltMappingsBasePlugin's configureEach
 */
public abstract class CompressTinyTask extends DefaultMappingsTask implements
        ArtifactFileTask, QuiltMappingsArtifactTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapMinecraftJarsPlugin}.
     */
    public static final String COMPRESS_TINY_TASK_NAME = "compressTiny";

    public static final String TINY_CLASSIFIER = "tiny";

    public static final String DEFAULT_EXTENSION = "gz";

    @InputFile
    public abstract RegularFileProperty getMappings();

    public CompressTinyTask() {
        super(Groups.BUILD_MAPPINGS);

        this.getArtifactExtension().convention(DEFAULT_EXTENSION);
    }

    @TaskAction
    public void compressTiny() throws IOException {
        this.getLogger().lifecycle(":compressing tiny mappings");

        try (
            final var outputStream =
                new GZIPOutputStream(new FileOutputStream(this.getArtifactFile().get().getAsFile()));
            final var inputStream = new FileInputStream(this.getMappings().get().getAsFile())
        ) {
            final byte[] buffer = new byte[1024];

            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.finish();
        }
    }
}
