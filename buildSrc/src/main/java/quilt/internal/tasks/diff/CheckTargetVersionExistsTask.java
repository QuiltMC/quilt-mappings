package quilt.internal.tasks.diff;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.StreamSupport;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public class CheckTargetVersionExistsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "checkTargetVersionExists";

    @Internal
    private Optional<String> targetVersion = Optional.empty();

    @OutputFile
    private final File metaFile;

    public CheckTargetVersionExistsTask() {
        super("diff");
        this.outputsNeverUpToDate();
        metaFile = new File(fileConstants.cacheFilesMinecraft, "quilt-mappings-" + Constants.MINECRAFT_VERSION + ".json");
    }

    @TaskAction
    public void checkExists() {
        try {
            this.startDownload()
                    .src("https://meta.quiltmc.org/v3/versions/quilt-mappings/" + Constants.MINECRAFT_VERSION)
                    .dest(metaFile)
                    .overwrite(true)
                    .download();

            JsonElement parsed = JsonParser.parseReader(new FileReader(metaFile));
            targetVersion = StreamSupport.stream(parsed.getAsJsonArray().spliterator(), false)
                    .max(Comparator.comparing(element -> element.getAsJsonObject().get("build").getAsInt(), Integer::compare))
                    .map(element -> element.getAsJsonObject().get("version").getAsString());
        } catch (IOException e) {
            targetVersion = Optional.empty();
        }
    }

    public Optional<String> getTargetVersion() {
        return targetVersion;
    }

    public File getMetaFile() {
        return metaFile;
    }
}
