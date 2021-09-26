package quilt.internal.tasks.mappings;

import java.io.File;
import java.util.List;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import quilt.internal.Constants;
import quilt.internal.FileConstants;
import quilt.internal.MappingsPlugin;
import quilt.internal.tasks.AbstractMappingsTask;

public class MappingsTask extends JavaExec implements AbstractMappingsTask {
    private final FileConstants fileConstants;

    public MappingsTask() {
        this.setGroup(Constants.Groups.MAPPINGS_GROUP);
        this.fileConstants = MappingsPlugin.getExtension(getProject()).getFileConstants();
        this.getMainClass().set("cuchaz.enigma.gui.Main");
        this.classpath(getProject().getConfigurations().getByName("enigmaRuntime"));
        jarToMap = getObjectFactory().property(File.class);
        jvmArgs("-Xmx2048m");
    }

    @Override
    public void exec() {
        args(List.of(
                "-jar", this.jarToMap.get().getAbsolutePath(), "-mappings", fileConstants.mappingsDir.getAbsolutePath(), "-profile", "enigma_profile.json"
        ));
        super.exec();
    }

    @InputFile
    private final Property<File> jarToMap;

    public void setJarToMap(File jarToMap) {
        this.jarToMap.set(jarToMap);
    }

    public File getJarToMap() {
        return jarToMap.get();
    }
}
