package quilt.internal.tasks.setup;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.jvm.tasks.Jar;
import quilt.internal.Constants.Groups;
import quilt.internal.tasks.MappingsTask;

public abstract class ConstantsJarTask extends Jar implements MappingsTask {
    public static final String CONSTANTS_JAR_TASK_NAME = "constantsJar";

    @InputFiles
    public abstract ConfigurableFileCollection getConstants();

    public ConstantsJarTask() {
        this.setGroup(Groups.SETUP);

        this.getArchiveClassifier().convention("constants");

        this.from(this.getConstants());
    }
}
