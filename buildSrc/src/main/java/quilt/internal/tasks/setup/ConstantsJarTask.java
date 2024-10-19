package quilt.internal.tasks.setup;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.SourceSet;
import org.gradle.jvm.tasks.Jar;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;

public abstract class ConstantsJarTask extends Jar implements MappingsTask {
    public static final String CONSTANTS_JAR_TASK_NAME = "constantsJar";

    @InputFiles
    public abstract ConfigurableFileCollection getConstants();

    public ConstantsJarTask() {
        this.setGroup(Constants.Groups.SETUP);

        this.getArchiveClassifier().convention("constants");

        this.from(this.getConstants());
    }
}
