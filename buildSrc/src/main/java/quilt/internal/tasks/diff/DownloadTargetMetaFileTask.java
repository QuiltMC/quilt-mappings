package quilt.internal.tasks.diff;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Provider;
import org.gradle.work.DisableCachingByDefault;
import quilt.internal.Constants;
import quilt.internal.Constants.Groups;
import quilt.internal.tasks.SimpleDownloadTask;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Comparator;
import java.util.stream.StreamSupport;

/**
 * @see TargetVersionConsumingTask
 */
@DisableCachingByDefault(because = "Output depends on a remote source that may change.")
public abstract class DownloadTargetMetaFileTask extends SimpleDownloadTask {
    public static final String DOWNLOAD_TARGET_META_FILE_TASK_NAME = "downloadTargetMetaFile";

    public Provider<String> provideTargetVersion() {
        return this.getDest().map(metaFile -> {
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
        });
    }

    public DownloadTargetMetaFileTask() {
        super(Groups.DIFF);

        this.getUrl().convention(
            "https://meta.quiltmc.org/v3/versions/quilt-mappings/" + Constants.MINECRAFT_VERSION
        );
    }
}
