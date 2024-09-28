package quilt.internal.tasks.setup;

import org.apache.commons.io.FileUtils;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import java.io.File;
import java.io.IOException;

public abstract class AbstractDownloadMappingsTask extends DefaultMappingsTask {
    @OutputFile
    public abstract RegularFileProperty getJarFile();

    @OutputFile
    public abstract RegularFileProperty getTinyFile();

    private final String mappingsName;

    public AbstractDownloadMappingsTask(String mappingsName) {
        super(Constants.Groups.SETUP_GROUP);
        this.getJarFile().convention(() -> new File(
            this.fileConstants.cacheFilesMinecraft,
            "%s-%s.jar".formatted(Constants.MINECRAFT_VERSION, mappingsName)
        ));
        this.getTinyFile().convention(() -> new File(
            this.fileConstants.cacheFilesMinecraft,
            "%s-%s.tiny".formatted(Constants.MINECRAFT_VERSION, mappingsName)
        ));
        
        this.mappingsName = mappingsName;
    }

    @TaskAction
    public void downloadMappings() throws IOException {
        this.startDownload()
                // TODO eliminate project access in task action
                .src(this.getProject().getConfigurations().getByName(this.mappingsName).resolve().iterator().next().toURI().toString())
                .dest(this.getJarFile().get().getAsFile())
                .overwrite(false)
                .download();

        FileUtils.copyFile(
            // TODO eliminate project access in task action
            this.getProject()
                .zipTree(this.getJarFile().get().getAsFile())
                .getFiles()
                .stream()
                .filter(file -> file.getName().endsWith("mappings.tiny"))
                .findFirst()
                .orElseThrow(),
            this.getTinyFile().get().getAsFile()
        );
    }
}
