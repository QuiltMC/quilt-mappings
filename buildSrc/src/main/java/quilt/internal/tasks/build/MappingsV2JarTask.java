package quilt.internal.tasks.build;

import java.io.File;
import java.util.Map;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.jvm.tasks.Jar;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;

public class MappingsV2JarTask extends Jar implements MappingsTask {
    @InputFile
    private final RegularFileProperty mappings;

    @InputFile
    private final RegularFileProperty unpickFile;

    public MappingsV2JarTask() {
        this.setGroup(Constants.Groups.BUILD_MAPPINGS_GROUP);
        this.outputsNeverUpToDate();
        getDestinationDirectory().set(getProject().file("build/libs"));

        File unpickMetaFile = mappingsExt().getFileConstants().unpickMeta;
        from(unpickMetaFile, copySpec -> {
            copySpec.expand(Map.of("version", getProject().property("unpick_version")));
            copySpec.rename(unpickMetaFile.getName(), "extras/unpick.json");
        });

        this.unpickFile = getObjectFactory().fileProperty();
        from(this.unpickFile, copySpec -> copySpec.rename(name -> "extras/definitions.unpick"));

        this.mappings = getObjectFactory().fileProperty();
        from(this.mappings, copySpec -> copySpec.rename(name -> "mappings/mappings.tiny"));
    }

    public RegularFileProperty getMappings() {
        return this.mappings;
    }


    public RegularFileProperty getUnpickFile() {
        return this.unpickFile;
    }
}
