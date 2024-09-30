package quilt.internal.tasks.setup;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import java.io.IOException;

public abstract class DownloadMappingsTask extends DefaultMappingsTask {
    @InputFiles
    public abstract Property<Configuration> getMappingsConfiguration();

    @OutputFile
    public abstract RegularFileProperty getJarFile();

    public DownloadMappingsTask() {
        super(Constants.Groups.SETUP_GROUP);
    }

    @TaskAction
    public void downloadMappings() throws IOException {
        this.startDownload()
                .src(this.getMappingsConfiguration().get().resolve().iterator().next().toURI().toString())
                .dest(this.getJarFile().get().getAsFile())
                .overwrite(false)
                .download();
    }
}
