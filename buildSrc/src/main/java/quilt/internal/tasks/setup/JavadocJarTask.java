package quilt.internal.tasks.setup;

import org.gradle.api.tasks.bundling.Jar;
import quilt.internal.Constants;
import quilt.internal.Constants.Groups;
import quilt.internal.tasks.MappingsTask;

public abstract class JavadocJarTask extends Jar implements MappingsTask {
    public static final String JAVADOC_JAR_TASK_NAME = "javadocJar";

    public JavadocJarTask() {
        this.setGroup(Groups.JAVADOC_GENERATION);

        this.getArchiveVersion().convention(Constants.MAPPINGS_VERSION);

        this.getArchiveClassifier().convention("javadoc");
    }
}
