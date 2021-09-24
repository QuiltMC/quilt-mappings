package quilt.internal.tasks.build;

import org.gradle.jvm.tasks.Jar;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;

public class TinyJarTask extends Jar implements MappingsTask {
    public static final String TASK_NAME = "tinyJar";

    public TinyJarTask() {
        setGroup(Constants.Groups.BUILD_MAPPINGS_GROUP);
        dependsOn(MergeTinyTask.TASK_NAME);
        outputsNeverUpToDate();
        getArchiveFileName().set(String.format("%s-%s.jar", Constants.MAPPINGS_NAME, Constants.MAPPINGS_VERSION));
        getDestinationDirectory().set(getProject().file("build/libs"));
        this.getArchiveClassifier().convention("");
        from(this.getTaskFromType(MergeTinyTask.class).mergedTiny).rename(original -> "mappings/mappings.tiny");
    }
}
