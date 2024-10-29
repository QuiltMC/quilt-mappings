package quilt.internal.task.setup;

import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Jar;
import quilt.internal.constants.Classifiers;
import quilt.internal.constants.Groups;
import quilt.internal.plugin.ProcessMappingsPlugin;
import quilt.internal.task.MappingsTask;

public abstract class JavadocJarTask extends Jar implements MappingsTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link ProcessMappingsPlugin}.
     */
    public static final String JAVADOC_JAR_TASK_NAME = "javadocJar";

    public JavadocJarTask() {
        this.setGroup(Groups.JAVADOC_GENERATION);

        this.getArchiveClassifier().convention(Classifiers.JAVADOC);
    }
}
