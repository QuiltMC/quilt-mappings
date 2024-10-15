package quilt.internal.tasks.diff;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.stream.StreamSupport;

/**
 * @see TargetVersionConsumingTask
 */
public abstract class DownloadTargetMetaFileTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadTargetMetaFile";

    @OutputFile
    public abstract RegularFileProperty getMetaFile();

    public Provider<String> provideTargetVersion() {
        // TODO does putting the mapped provider in this property make it cache?
        return this.getObjects().property(String.class)
            .convention(this.getMetaFile().map(metaFile -> {
                final JsonElement parsed;
                try {
                    parsed = JsonParser.parseReader(new FileReader(metaFile.getAsFile()));
                } catch (FileNotFoundException e) {
                    throw new GradleException("Failed to open meta file", e);
                }

                return StreamSupport.stream(parsed.getAsJsonArray().spliterator(), false)
                    .max(Comparator.comparing(
                        element -> element.getAsJsonObject().get("build").getAsInt(),
                        Integer::compare
                    ))
                    .map(element -> element.getAsJsonObject().get("version").getAsString())
                    .orElse(null);
            }));
    }

    @Inject
    protected abstract ObjectFactory getObjects();

    public DownloadTargetMetaFileTask() {
        super("diff");
    }

    @TaskAction
    public void downloadMetaFile() {
        try {
            final File metaFile = this.getMetaFile().get().getAsFile();

            this.startDownload()
                    .src("https://meta.quiltmc.org/v3/versions/quilt-mappings/" + Constants.MINECRAFT_VERSION)
                    .dest(metaFile)
                    .overwrite(true)
                    .download();

        } catch (IOException e) {
            this.getLogger().error("Failed to read target version", e);
        }
    }
}
