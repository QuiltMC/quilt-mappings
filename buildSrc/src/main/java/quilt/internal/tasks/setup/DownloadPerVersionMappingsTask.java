package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public class DownloadPerVersionMappingsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadPerVersionMappings";

    @OutputFile
    public File jarFile = new File(fileConstants.cacheFilesMinecraft, String.format("%s-%s.jar", Constants.MINECRAFT_VERSION, Constants.PER_VERSION_MAPPINGS_NAME));

    @OutputFile
    public File tinyFile = new File(fileConstants.cacheFilesMinecraft, String.format("%s-%s.tiny", Constants.MINECRAFT_VERSION, Constants.PER_VERSION_MAPPINGS_NAME));

    public DownloadPerVersionMappingsTask() {
        super(Constants.Groups.SETUP_GROUP);
    }

    @TaskAction
    public void downloadPerVersionMappings() throws IOException {
        startDownload()
                .src(getProject().getConfigurations().getByName(Constants.PER_VERSION_MAPPINGS_NAME).resolve().iterator().next().toURI().toString())
                .dest(jarFile)
                .overwrite(false)
                .download();

        FileUtils.copyFile(getProject()
                .zipTree(jarFile)
                .getFiles()
                .stream()
                .filter(file -> file.getName().endsWith("mappings.tiny"))
                .findFirst()
                .get(), tinyFile);
    }

    public File getJarFile() {
        return jarFile;
    }

    public File getTinyFile() {
        return tinyFile;
    }
}
