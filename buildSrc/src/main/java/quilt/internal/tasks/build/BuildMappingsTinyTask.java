package quilt.internal.tasks.build;

import java.io.File;
import java.io.IOException;

import cuchaz.enigma.command.MapSpecializedMethodsCommand;
import cuchaz.enigma.translation.mapping.serde.MappingParseException;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.tasks.jarmapping.MapPerVersionMappingsJarTask;

public class BuildMappingsTinyTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "buildMappingsTiny";

    @OutputFile
    public File outputMappings = new File(fileConstants.tempDir, String.format("%s.tiny", Constants.MAPPINGS_NAME));

    public BuildMappingsTinyTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        dependsOn(MapPerVersionMappingsJarTask.TASK_NAME);
        getInputs().dir(fileConstants.mappingsDir);
        outputsNeverUpToDate();
    }

    @TaskAction
    public void buildMappingsTiny() throws IOException, MappingParseException {
        getLogger().lifecycle(":generating tiny mappings");

        new MapSpecializedMethodsCommand().run(
                fileConstants.hashedMojmapJar.getAbsolutePath(),
                "enigma",
                fileConstants.mappingsDir.getAbsolutePath(),
                String.format("tinyv2:%s:named", Constants.PER_VERSION_MAPPINGS_NAME),
                outputMappings.getAbsolutePath()
        );
    }

    public File getOutputMappings() {
        return outputMappings;
    }
}
