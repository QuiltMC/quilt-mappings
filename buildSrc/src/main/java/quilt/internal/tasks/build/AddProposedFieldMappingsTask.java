package quilt.internal.tasks.build;

import java.io.File;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import net.fabricmc.stitch.commands.tinyv2.CommandProposeV2FieldNames;

public class AddProposedFieldMappingsTask extends DefaultMappingsTask {
    @OutputFile
    public File outputMappings;

    @InputFile
    private final Property<File> inputJar;

    @InputFile
    private final Property<File> inputMappings;

    public AddProposedFieldMappingsTask() {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);
        outputMappings = new File(fileConstants.tempDir, getName() + "-with-enum.tiny");
        outputsNeverUpToDate();
        inputJar = getProject().getObjects().property(File.class);
        inputMappings = getProject().getObjects().property(File.class);
    }

    @TaskAction
    public void addProposedFieldMappings() throws Exception {
        getLogger().lifecycle(":seeking auto-mappable fields mappings");

        String[] argsProposeV2 = {
                inputJar.get().getAbsolutePath(),
                inputMappings.get().getAbsolutePath(),
                outputMappings.getAbsolutePath(),
                "false"
        };

        new CommandProposeV2FieldNames().run(argsProposeV2);
    }

    public File getOutputMappings() {
        return outputMappings;
    }

    public AddProposedFieldMappingsTask setOutputMappings(File outputMappings) {
        this.outputMappings = outputMappings;
        return this;
    }

    public Property<File> getInputJar() {
        return inputJar;
    }

    public Property<File> getInputMappings() {
        return inputMappings;
    }
}
