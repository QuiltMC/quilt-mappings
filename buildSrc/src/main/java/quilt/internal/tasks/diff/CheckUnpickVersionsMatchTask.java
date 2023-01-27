package quilt.internal.tasks.diff;

import java.io.FileReader;
import java.io.IOException;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.gradle.api.artifacts.VersionConstraint;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.tasks.DefaultMappingsTask;

public class CheckUnpickVersionsMatchTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "checkUnpickVersionsMatch";

    @InputFile
    private final RegularFileProperty unpickJson;

    @Internal
    private boolean match = false;

    public CheckUnpickVersionsMatchTask() {
        super("diff");

        this.dependsOn(DownloadTargetMappingJarTask.TASK_NAME);

        this.onlyIf(task -> this.getTaskByType(CheckTargetVersionExistsTask.class).getTargetVersion().isPresent());

        unpickJson = getProject().getObjects().fileProperty();
        unpickJson.convention(getTaskByType(DownloadTargetMappingJarTask.class).getTargetUnpickFile());
    }

    @TaskAction
    public void checkMatch() throws IOException {
        JsonElement parsed = JsonParser.parseReader(new FileReader(unpickJson.getAsFile().get()));
        match = parsed.getAsJsonObject().get("unpickVersion").getAsString().equals(libs().findVersion("unpick").map(VersionConstraint::getRequiredVersion).orElse(""));
    }

    public boolean isMatch() {
        return match;
    }

    public RegularFileProperty getUnpickJson() {
        return unpickJson;
    }
}
