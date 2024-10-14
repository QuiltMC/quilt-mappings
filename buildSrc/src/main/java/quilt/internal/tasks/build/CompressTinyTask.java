package quilt.internal.tasks.build;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public abstract class CompressTinyTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "compressTiny";

    @OutputFile
    public abstract RegularFileProperty getCompressedTiny();

    @InputFile
    public abstract RegularFileProperty getMappings();

    public CompressTinyTask() {
        super(Constants.Groups.BUILD_MAPPINGS);
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
