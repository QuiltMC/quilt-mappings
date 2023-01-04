package quilt.internal.tasks.jarmapping;

import java.util.Map;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.util.JarRemapper;

public abstract class MapJarTask extends DefaultMappingsTask {
    @InputFile
    protected final RegularFileProperty inputJar;

    @InputFile
    protected final RegularFileProperty mappingsFile;

    @OutputFile
    protected final RegularFileProperty outputJar;

    private final String from, to;

    public MapJarTask(String group, String from, String to) {
        super(group);
        this.from = from;
        this.to = to;

        inputJar = getProject().getObjects().fileProperty();
        mappingsFile = getProject().getObjects().fileProperty();
        outputJar = getProject().getObjects().fileProperty();
    }

    @TaskAction
    public void remapJar() {
        getLogger().lifecycle(":mapping minecraft from " + from + " to " + to);
        Map<String, String> additionalMappings = getAdditionalMappings();
        JarRemapper.mapJar(outputJar.getAsFile().get(), inputJar.getAsFile().get(), mappingsFile.get().getAsFile(), fileConstants.libraries, from, to, builder -> builder.withMappings(out -> additionalMappings.forEach(out::acceptClass)));
    }

    @Internal
    public Map<String, String> getAdditionalMappings() {
        return Map.of();
    }

    public RegularFileProperty getInputJar() {
        return inputJar;
    }

    public RegularFileProperty getMappingsFile() {
        return mappingsFile;
    }

    public RegularFileProperty getOutputJar() {
        return outputJar;
    }
}
