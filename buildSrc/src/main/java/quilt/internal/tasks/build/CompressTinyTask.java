package quilt.internal.tasks.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public class CompressTinyTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "compressTiny";

    @OutputFile
    public File compressedTiny = new File(fileConstants.libs, String.format("%s-tiny-%s.gz", Constants.MAPPINGS_NAME, Constants.MAPPINGS_VERSION));

    public CompressTinyTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        dependsOn(TinyJarTask.TASK_NAME, MergeTinyTask.TASK_NAME);
        getOutputs().file(compressedTiny);
        outputsNeverUpToDate();
    }

    @TaskAction
    public void compressTiny() throws IOException {
        getLogger().lifecycle(":compressing tiny mappings");

        byte[] buffer = new byte[1024];
        FileOutputStream fileOutputStream = new FileOutputStream(compressedTiny);
        GZIPOutputStream outputStream = new GZIPOutputStream(fileOutputStream);
        FileInputStream fileInputStream = new FileInputStream(this.getTaskFromType(MergeTinyTask.class).mergedTiny);

        int length;
        while ((length = fileInputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        fileInputStream.close();
        outputStream.finish();
        outputStream.close();
    }

    public File getCompressedTiny() {
        return compressedTiny;
    }
}
