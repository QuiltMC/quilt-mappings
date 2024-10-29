package quilt.internal.task.setup;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.jvm.tasks.Jar;
import quilt.internal.constants.Classifiers;
import quilt.internal.constants.Groups;
import quilt.internal.plugin.ProcessMappingsPlugin;
import quilt.internal.task.MappingsTask;

public abstract class SourcesJarTask extends Jar implements MappingsTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link ProcessMappingsPlugin}.
     */
    public static final String SOURCES_JAR_TASK_NAME = "sourcesJar";

    @InputFiles
    public abstract ConfigurableFileCollection getSources();

    public SourcesJarTask() {
        this.setGroup(Groups.SETUP);

        this.getArchiveClassifier().convention(Classifiers.SOURCES);

        this.from(this.getSources());
    }
}
