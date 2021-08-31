package quilt.internal.tasks.build;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public class DownloadPerVersionMappingsTask extends DefaultMappingsTask {
    @OutputFile
    public File jarFile = new File(fileConstants.cacheFilesMinecraft, Constants.MINECRAFT_VERSION + "-" + Constants.PER_VERSION_MAPPINGS_NAME + ".jar");

    @OutputFile
    public File tinyFile = new File(fileConstants.cacheFilesMinecraft, Constants.MINECRAFT_VERSION + "-" + Constants.PER_VERSION_MAPPINGS_NAME + ".tiny");

    public DownloadPerVersionMappingsTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
    }

    @TaskAction
    public void downloadPerVersionMappings() throws IOException {
        startDownload()
                .src(getProject().getConfigurations().getByName("hashed").resolve().iterator().next().toURI().toString())
                .dest(jarFile)
                .overwrite(false)
                .download();

        FileUtils.copyFile(getProject().zipTree(jarFile).getFiles().stream().filter(file -> file.getName().endsWith("mappings.tiny")).findFirst().get(), tinyFile);
    }

    public File getJarFile() {
        return jarFile;
    }

    public File getTinyFile() {
        return tinyFile;
    }
}
