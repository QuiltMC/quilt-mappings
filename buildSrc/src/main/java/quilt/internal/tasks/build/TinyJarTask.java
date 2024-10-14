package quilt.internal.tasks.build;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.jvm.tasks.Jar;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;

public abstract class TinyJarTask extends Jar implements MappingsTask {
    public static final String TASK_NAME = "tinyJar";

    public static final String JAR_MAPPINGS_PATH = "mappings/mappings.tiny";

    @InputFile
    public abstract RegularFileProperty getMappings();

    public TinyJarTask() {
        this.setGroup(Constants.Groups.BUILD_MAPPINGS);

        this.from(this.getMappings()).rename(original -> JAR_MAPPINGS_PATH);
    }
}
