package quilt.internal.tasks.diff;

import java.io.FileReader;
import java.io.IOException;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.VersionConstraint;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.tasks.DefaultMappingsTask;

// TODO see if this can be replaced with a ValueSource or a BuildService
/**
 * @see UnpickVersionsMatchConsumingTask
 */
public abstract class CheckUnpickVersionsMatchTask extends DefaultMappingsTask implements TargetVersionConsumingTask {
    public static final String TASK_NAME = "checkUnpickVersionsMatch";

    @InputFile
    public abstract RegularFileProperty getUnpickMeta();

    @Internal
    protected abstract Property<Boolean> getMatch();

    public CheckUnpickVersionsMatchTask() {
        super("diff");

        // provide an informative error message if isMatch is accessed incorrectly
        this.getMatch().convention(this.getProject().provider(() -> {
             throw new GradleException(
                 "targetVersion has not been populated. " +
                     "Its should only be accessed from others tasks' inputs or predicates and " +
                     "only after checking if it's present."
             );
        }));
    }

    @TaskAction
    public void checkMatch() throws IOException {
        final JsonElement parsed = JsonParser.parseReader(new FileReader(this.getUnpickMeta().getAsFile().get()));
        this.getMatch().set(
            parsed.getAsJsonObject().get("unpickVersion").getAsString().equals(
                this.libs().findVersion("unpick").map(VersionConstraint::getRequiredVersion).orElse("")
            )
        );
    }

    /**
     * This is only populated after the task has run.
     * <p>
     * It should only be accessed from other tasks' {@linkplain TaskAction actions} or
     * {@linkplain org.gradle.api.Task#onlyIf predicates} and via
     * {@linkplain Property lazy} {@linkplain org.gradle.api.tasks.Input input}.
     * <p>
     * It may be absent even after this task has run, so always check {@link Provider#isPresent() isPresent}.
     */
    public Provider<Boolean> isMatch() {
        return this.getMatch();
    }
}
