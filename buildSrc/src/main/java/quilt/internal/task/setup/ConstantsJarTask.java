package quilt.internal.task.setup;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.jvm.tasks.Jar;
import quilt.internal.constants.Classifiers;
import quilt.internal.constants.Groups;
import quilt.internal.plugin.MapV2Plugin;
import quilt.internal.task.MappingsTask;

public abstract class ConstantsJarTask extends Jar implements MappingsTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link MapV2Plugin}.
     */
    public static final String CONSTANTS_JAR_TASK_NAME = "constantsJar";

    @InputFiles
    public abstract ConfigurableFileCollection getConstants();

    public ConstantsJarTask() {
        this.setGroup(Groups.SETUP);

        this.getArchiveClassifier().convention(Classifiers.CONSTANTS);

        this.from(this.getConstants());
    }
}
