package quilt.internal.tasks.build;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.jvm.tasks.Jar;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;

public abstract class TinyJarTask extends Jar implements MappingsTask {
    public static final String TASK_NAME = "tinyJar";

    @InputFile
    public abstract RegularFileProperty getMappings();

    public TinyJarTask() {
        this.setGroup(Constants.Groups.BUILD_MAPPINGS_GROUP);
        this.dependsOn(MergeTinyTask.TASK_NAME);

        this.getMappings().convention(
            this.getTaskNamed(MergeTinyTask.TASK_NAME, MergeTinyTask.class).getOutputMappings()
        );

        this.getArchiveFileName().set(String.format("%s-%s.jar", Constants.MAPPINGS_NAME, Constants.MAPPINGS_VERSION));
        this.getDestinationDirectory().set(this.getProject().file("build/libs"));
        this.getArchiveClassifier().convention("");
        this.from(this.getMappings()).rename(original -> "mappings/mappings.tiny");
    }
}
