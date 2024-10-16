package quilt.internal.tasks.setup;

import org.gradle.jvm.tasks.Jar;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;

public abstract class ConstantsJarTask extends Jar implements MappingsTask {
    public static final String CONSTANTS_JAR_TASK_NAME = "constantsJar";

    public ConstantsJarTask() {
        this.setGroup(Constants.Groups.SETUP);
    }
}
