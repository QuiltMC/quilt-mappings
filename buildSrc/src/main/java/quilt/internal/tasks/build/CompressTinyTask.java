package quilt.internal.tasks.build;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskContainer;
import quilt.internal.Constants.Groups;
import quilt.internal.plugin.MapMinecraftJarsPlugin;
import quilt.internal.tasks.DefaultMappingsTask;

public abstract class CompressTinyTask extends DefaultMappingsTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapMinecraftJarsPlugin}.
     */
    public static final String COMPRESS_TINY_TASK_NAME = "compressTiny";

    @InputFile
    public abstract RegularFileProperty getMappings();

    @OutputFile
    public abstract RegularFileProperty getCompressedTiny();

    public CompressTinyTask() {
        super(Groups.BUILD_MAPPINGS);
    }

    @TaskAction
    public void compressTiny() throws IOException {
        this.getLogger().lifecycle(":compressing tiny mappings");

        try (
            final var outputStream =
                new GZIPOutputStream(new FileOutputStream(this.getCompressedTiny().get().getAsFile()));
            final var fileInputStream = new FileInputStream(this.getMappings().get().getAsFile())
        ) {
            final byte[] buffer = new byte[1024];

            int length;
            while ((length = fileInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.finish();
        }
    }
}
