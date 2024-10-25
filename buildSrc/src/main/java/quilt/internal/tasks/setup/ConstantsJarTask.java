package quilt.internal.tasks.setup;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.jvm.tasks.Jar;
import quilt.internal.Constants.Groups;
import quilt.internal.plugin.MapV2Plugin;
import quilt.internal.tasks.MappingsTask;

// TODO move this back to build.gradle?
public abstract class ConstantsJarTask extends Jar implements MappingsTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapV2Plugin}.
     */
    public static final String CONSTANTS_JAR_TASK_NAME = "constantsJar";

    public static final String CONSTANTS_CLASSIFIER = "constants";

    @InputFiles
    public abstract ConfigurableFileCollection getConstants();

    public ConstantsJarTask() {
        this.setGroup(Groups.SETUP);

        this.getArchiveClassifier().convention(CONSTANTS_CLASSIFIER);

        this.from(this.getConstants());
    }
}
