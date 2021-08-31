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
    @OutputFile
    public File compressedTiny = new File(fileConstants.libs, Constants.MAPPINGS_NAME + "-tiny-"+ Constants.MAPPINGS_VERSION +".gz");

    public CompressTinyTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        dependsOn("tinyJar", "mergeTiny");
        getOutputs().file(compressedTiny);
        outputsNeverUpToDate();
    }

    @TaskAction
    public void compressTiny() throws IOException {
        getLogger().lifecycle(":compressing tiny mappings");

        byte[] buffer = new byte[1024];
        FileOutputStream fileOutputStream = new FileOutputStream(compressedTiny);
        GZIPOutputStream outputStream = new GZIPOutputStream(fileOutputStream);
        FileInputStream fileInputStream = new FileInputStream(this.<MergeTinyTask>getTaskFromName("mergeTiny").mergedTiny);

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
