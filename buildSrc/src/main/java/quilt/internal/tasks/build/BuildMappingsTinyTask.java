package quilt.internal.tasks.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import cuchaz.enigma.command.MapSpecializedMethodsCommand;
import cuchaz.enigma.translation.mapping.serde.MappingParseException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.jarmapping.MapPerVersionMappingsJarTask;

public class BuildMappingsTinyTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "buildMappingsTiny";
    @InputDirectory
    private final RegularFileProperty mappings;

    @OutputFile
    public File outputMappings = new File(fileConstants.tempDir, String.format("%s.tiny", Constants.MAPPINGS_NAME));

    public BuildMappingsTinyTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        dependsOn(MapPerVersionMappingsJarTask.TASK_NAME);
        mappings = getProject().getObjects().fileProperty();
        mappings.set(getProject().file("mappings"));
    }

    @TaskAction
    public void execute() throws IOException, MappingParseException {
        getLogger().lifecycle(":generating tiny mappings");

        buildMappingsTiny(
                fileConstants.perVersionMappingsJar.toPath(),
                mappings.get().getAsFile().toPath(),
                outputMappings.toPath()
        );
    }

    public static void buildMappingsTiny(Path perVersionMappingsJar, Path mappings, Path outputMappings) throws IOException, MappingParseException {
        MapSpecializedMethodsCommand.run(
                perVersionMappingsJar,
                "enigma",
                mappings,
                String.format("tinyv2:%s:named", Constants.PER_VERSION_MAPPINGS_NAME),
                outputMappings
        );
    }

    public File getOutputMappings() {
        return outputMappings;
    }

    public RegularFileProperty getMappings() {
        return mappings;
    }
}
