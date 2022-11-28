package quilt.internal.tasks.unpick;

import java.io.File;
import java.util.List;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;
import quilt.internal.tasks.jarmapping.MapPerVersionMappingsJarTask;

public class UnpickJarTask extends JavaExec implements MappingsTask {
    private final RegularFileProperty inputFile;
    private final Property<File> outputFile;
    private final RegularFileProperty unpickDefinition;
    private final RegularFileProperty unpickConstantsJar;

    public UnpickJarTask() {
        this.setGroup(Constants.Groups.UNPICK);

        this.getMainClass().set("daomephsta.unpick.cli.Main");
        classpath(getProject().getConfigurations().getByName("unpick"));

        dependsOn(MapPerVersionMappingsJarTask.TASK_NAME, "constantsJar", getTaskByName(RemapUnpickDefinitionsTask.TASK_NAME));

        ObjectFactory objectFactory = getProject().getObjects();
        inputFile = objectFactory.fileProperty();
        outputFile = objectFactory.property(File.class);
        unpickDefinition = objectFactory.fileProperty();
        unpickConstantsJar = objectFactory.fileProperty();
    }

    @Override
    public void exec() {
        args(List.of(
                inputFile.get().getAsFile().getAbsolutePath(), outputFile.get().getAbsolutePath(), unpickDefinition.get().getAsFile().getAbsolutePath(), unpickConstantsJar.get().getAsFile().getAbsolutePath()
        ));
        args(getProject().getConfigurations().getByName("decompileClasspath").getFiles());
        super.exec();
    }

    @InputFile
    public RegularFileProperty getInputFile() {
        return inputFile;
    }

    @Input
    public Property<File> getOutputFile() {
        return outputFile;
    }

    @InputFile
    public RegularFileProperty getUnpickDefinition() {
        return unpickDefinition;
    }

    @InputFile
    public RegularFileProperty getUnpickConstantsJar() {
        return unpickConstantsJar;
    }
}
