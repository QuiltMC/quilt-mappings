package quilt.internal.tasks.build;

import org.gradle.jvm.tasks.Jar;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;

public class TinyJarTask extends Jar implements MappingsTask {
    public TinyJarTask() {
        setGroup(Constants.Groups.BUILD_MAPPINGS_GROUP);
        dependsOn("mergeTiny");
        outputsNeverUpToDate();
        getArchiveFileName().set(Constants.MAPPINGS_NAME + "-" + Constants.MAPPINGS_VERSION + ".jar");
        getDestinationDirectory().set(getProject().file("build/libs"));
        setClassifier("");
        from(this.<MergeTinyTask>getTaskFromName("mergeTiny").mergedTiny).rename(original -> "mappings/mappings.tiny");
    }
}
