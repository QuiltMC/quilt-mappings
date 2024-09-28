package quilt.internal.tasks.build;

import java.io.File;
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
    protected abstract RegularFileProperty getMappings();

    public CompressTinyTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        this.dependsOn(TinyJarTask.TASK_NAME, MergeTinyTask.TASK_NAME);

        this.getCompressedTiny().convention(() -> new File(
            this.getProject().file("build/libs/"),
            "%s-%s-tiny.gz".formatted(Constants.MAPPINGS_NAME, Constants.MAPPINGS_VERSION)
        ));

        this.getMappings()
            .convention(this.getTaskNamed(MergeTinyTask.TASK_NAME, MergeTinyTask.class).getOutputMappings());
    }

    @TaskAction
    public void compressTiny() throws IOException {
        this.getLogger().lifecycle(":compressing tiny mappings");

        final byte[] buffer = new byte[1024];
        final FileOutputStream fileOutputStream = new FileOutputStream(this.getCompressedTiny().get().getAsFile());
        final GZIPOutputStream outputStream = new GZIPOutputStream(fileOutputStream);
        final FileInputStream fileInputStream = new FileInputStream(this.getMappings().get().getAsFile());

        int length;
        while ((length = fileInputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        fileInputStream.close();
        outputStream.finish();
        outputStream.close();
    }
}
