package quilt.internal.task.setup;

import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Jar;
import quilt.internal.Constants.Groups;
import quilt.internal.plugin.ProcessMappingsPlugin;
import quilt.internal.task.MappingsTask;

public abstract class JavadocJarTask extends Jar implements MappingsTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link ProcessMappingsPlugin}.
     */
    public static final String JAVADOC_JAR_TASK_NAME = "javadocJar";

    public static final String JAVADOC_CLASSIFIER = "javadoc";

    public JavadocJarTask() {
        this.setGroup(Groups.JAVADOC_GENERATION);

        this.getArchiveClassifier().convention(JAVADOC_CLASSIFIER);
    }
}
