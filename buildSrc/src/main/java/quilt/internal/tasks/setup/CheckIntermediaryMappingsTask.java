package quilt.internal.tasks.setup;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import java.io.File;
import java.util.Set;

public class CheckIntermediaryMappingsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "checkIntermediaryMappings";

    @Internal
    private boolean present = false;

    public CheckIntermediaryMappingsTask() {
        super(Constants.Groups.SETUP_GROUP);
    }

    public boolean isPresent() {
        return this.present;
    }

    @TaskAction
    public void checkIntermediaryMappings() {
        Configuration configuration = getProject().getConfigurations().getByName("intermediary");
        Set<File> files;

        try {
            files = configuration.resolve();
        } catch (Exception e) {
            return;
        }

        this.present = !files.isEmpty();
    }
}
