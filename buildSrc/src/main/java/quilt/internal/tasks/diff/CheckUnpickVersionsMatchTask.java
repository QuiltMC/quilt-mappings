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

public abstract class CheckUnpickVersionsMatchTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "checkUnpickVersionsMatch";

    @InputFile
    public abstract RegularFileProperty getUnpickJson();

    @Internal
    private boolean match = false;

    public CheckUnpickVersionsMatchTask() {
        super("diff");

        this.dependsOn(DownloadTargetMappingJarTask.TASK_NAME);

        this.onlyIf(task ->
            this.getTaskNamed(CheckTargetVersionExistsTask.TASK_NAME, CheckTargetVersionExistsTask.class)
                .getTargetVersion().isPresent()
        );

        this.getUnpickJson().convention(
            this.getTaskNamed(DownloadTargetMappingJarTask.TASK_NAME, DownloadTargetMappingJarTask.class)
                .getTargetUnpickFile()
        );
    }

    @TaskAction
    public void checkMatch() throws IOException {
        final JsonElement parsed = JsonParser.parseReader(new FileReader(this.getUnpickJson().getAsFile().get()));
        this.match = parsed.getAsJsonObject().get("unpickVersion").getAsString().equals(
            this.libs().findVersion("unpick").map(VersionConstraint::getRequiredVersion).orElse("")
        );
    }

    public boolean isMatch() {
        return this.match;
    }
}
