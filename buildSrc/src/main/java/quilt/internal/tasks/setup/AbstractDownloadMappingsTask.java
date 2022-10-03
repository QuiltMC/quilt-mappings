package quilt.internal.tasks.setup;

import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import java.io.File;
import java.io.IOException;

public abstract class AbstractDownloadMappingsTask extends DefaultMappingsTask {
    @OutputFile
    public File jarFile;

    @OutputFile
    public File tinyFile;

    private final String mappingsName;

    public AbstractDownloadMappingsTask(String mappingsName) {
        super(Constants.Groups.SETUP_GROUP);
        jarFile = new File(fileConstants.cacheFilesMinecraft, String.format("%s-%s.jar", Constants.MINECRAFT_VERSION, mappingsName));
        tinyFile = new File(fileConstants.cacheFilesMinecraft, String.format("%s-%s.tiny", Constants.MINECRAFT_VERSION, mappingsName));
        
        this.mappingsName = mappingsName;
    }

    @TaskAction
    public void downloadMappings() throws IOException {
        startDownload()
                .src(getProject().getConfigurations().getByName(mappingsName).resolve().iterator().next().toURI().toString())
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
