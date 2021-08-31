package quilt.internal.tasks.build;

import java.io.File;
import java.io.IOException;

import cuchaz.enigma.command.MapSpecializedMethodsCommand;
import cuchaz.enigma.translation.mapping.serde.MappingParseException;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public class BuildMappingsTinyTask extends DefaultMappingsTask {
    @OutputFile
    public File outputMappings = new File(fileConstants.tempDir, "quilt-mappings.tiny");

    public BuildMappingsTinyTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        dependsOn("mapHashedMojmapJar");
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
                "tinyv2:" + Constants.PER_VERSION_MAPPINGS_NAME + ":named",
                outputMappings.getAbsolutePath()
        );
    }

    public File getOutputMappings() {
        return outputMappings;
    }
}
