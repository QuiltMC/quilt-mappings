package quilt.internal.tasks.build;

import java.io.File;
import java.util.Map;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.jvm.tasks.Jar;
import quilt.internal.Constants;
import quilt.internal.tasks.AbstractMappingsTask;

import net.fabricmc.filament.task.CombineUnpickDefinitionsTask;

public class MappingsV2JarTask extends Jar implements AbstractMappingsTask {
    @InputFile
    private final RegularFileProperty mappings;

    public MappingsV2JarTask() {
        this.setGroup(Constants.Groups.BUILD_MAPPINGS_GROUP);
        this.outputsNeverUpToDate();
        getDestinationDirectory().set(getProject().file("build/libs"));

        File unpickMetaFile = getProject().file("unpick-definitions/unpick.json");
        from(unpickMetaFile, copySpec -> {
            copySpec.expand(Map.of("version", getProject().property("unpick_version")));
            copySpec.rename(unpickMetaFile.getName(), "extras/unpick.json");
        });

        RegularFileProperty combineUnpickDefinitions = getTaskByType(CombineUnpickDefinitionsTask.class).getOutput();
        from(combineUnpickDefinitions, copySpec -> {
            copySpec.rename(combineUnpickDefinitions.get().getAsFile().getName(), "extras/definitions.unpick");
        });

        mappings = getObjectFactory().fileProperty();

        from(mappings, copySpec -> {
            copySpec.rename((originalName) -> "mappings/mappings.tiny");
        });

    }

    public RegularFileProperty getMappings() {
        return mappings;
    }


}
