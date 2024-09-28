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
    public static final Map<String, String> JAVAX_TO_JETBRAINS = Map.of(
            "javax/annotation/Nullable", "org/jetbrains/annotations/Nullable",
            "javax/annotation/Nonnull", "org/jetbrains/annotations/NotNull",
            "javax/annotation/concurrent/Immutable", "org/jetbrains/annotations/Unmodifiable"
    );
    @InputFile
    protected abstract RegularFileProperty getInputJar();

    @InputFile
    protected abstract RegularFileProperty getMappingsFile();

    @OutputFile
    protected abstract RegularFileProperty getOutputJar();

    private final String from, to;

    public MapJarTask(String group, String from, String to) {
        super(group);
        this.from = from;
        this.to = to;
    }

    @TaskAction
    public void remapJar() {
        this.getLogger().lifecycle(":mapping minecraft from " + this.from + " to " + this.to);
        final Map<String, String> additionalMappings = this.getAdditionalMappings();
        JarRemapper.mapJar(
            this.getOutputJar().getAsFile().get(),
            this.getInputJar().getAsFile().get(),
            this.getMappingsFile().get().getAsFile(),
            this.fileConstants.libraries,
            this.from, this.to,
            builder -> builder.withMappings(out -> additionalMappings.forEach(out::acceptClass))
        );
    }

    @Internal
    public Map<String, String> getAdditionalMappings() {
        return Map.of();
    }
}
