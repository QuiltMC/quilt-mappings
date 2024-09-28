package quilt.internal.tasks.mappings;

import java.util.List;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;

public abstract class EnigmaMappingsTask extends JavaExec implements MappingsTask {
    @InputFile
    public abstract RegularFileProperty getJarToMap();

    public EnigmaMappingsTask() {
        this.setGroup(Constants.Groups.MAPPINGS_GROUP);
        this.getMainClass().set("org.quiltmc.enigma.gui.Main");
        this.classpath(this.getProject().getConfigurations().getByName("enigmaRuntime"));
        this.jvmArgs("-Xmx2048m");
    }

    @Override
    public void exec() {
        this.args(List.of(
            "-jar", this.getJarToMap().get().getAsFile().getAbsolutePath(),
            // TODO eliminate project access in task action
            "-mappings", this.getProject().file("mappings").getAbsolutePath(),
            "-profile", "enigma_profile.json"
        ));

        super.exec();
    }
}
