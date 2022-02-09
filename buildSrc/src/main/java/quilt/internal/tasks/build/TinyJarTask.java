package quilt.internal.tasks.build;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.jvm.tasks.Jar;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;

public class TinyJarTask extends Jar implements MappingsTask {
    public static final String TASK_NAME = "tinyJar";

    @InputFile
    private final RegularFileProperty mappings;

    public TinyJarTask() {
        setGroup(Constants.Groups.BUILD_MAPPINGS_GROUP);
        dependsOn(MergeTinyTask.TASK_NAME);

        mappings = getProject().getObjects().fileProperty();
        mappings.convention(this.getTaskByType(MergeTinyTask.class)::getOutputMappings);

        getArchiveFileName().set(String.format("%s-%s.jar", Constants.MAPPINGS_NAME, Constants.MAPPINGS_VERSION));
        getDestinationDirectory().set(getProject().file("build/libs"));
        this.getArchiveClassifier().convention("");
        from(mappings).rename(original -> "mappings/mappings.tiny");
    }

    public RegularFileProperty getMappings() {
        return mappings;
    }
}
