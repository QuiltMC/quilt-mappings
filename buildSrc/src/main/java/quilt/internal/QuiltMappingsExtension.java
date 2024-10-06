package quilt.internal;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.quiltmc.enigma.api.EnigmaProfile;

import java.io.File;
import java.io.IOException;

public abstract class QuiltMappingsExtension {
    public static final String EXTENSION_NAME = "quiltMappings";

    // TODO see if this can use a BuildService
    /**
     * {@link QuiltMappingsPlugin} configures all
     * {@link quilt.internal.tasks.EnigmaProfileConsumingTask EnigmaProfileConsumingTask}s to use this profile.
     */
    public final Provider<EnigmaProfile> enigmaProfile;

    public abstract RegularFileProperty getUnpickMeta();

    private final FileConstants fileConstants;

    public static QuiltMappingsExtension get(Project project) {
        return project.getExtensions().getByType(QuiltMappingsExtension.class);
    }

    protected abstract RegularFileProperty getEnigmaProfileConfigImpl();

    public QuiltMappingsExtension(Project project) {
        this.fileConstants = new FileConstants(project);

        this.enigmaProfile = this.getEnigmaProfileConfigImpl()
            .map(RegularFile::getAsFile)
            .map(File::toPath)
            .map(profilePath -> {
                try {
                    return EnigmaProfile.read(profilePath);
                } catch (IOException e) {
                    throw new GradleException("Failed to read enigma profile", e);
                }
            });
    }

    public FileConstants getFileConstants() {
        return this.fileConstants;
    }

    // TODO is there a cleaner way to only expose setters?
    public void setEnigmaProfileConfig(File file) {
        this.getEnigmaProfileConfigImpl().set(file);
    }

    public void setEnigmaProfileConfig(RegularFile file) {
        this.getEnigmaProfileConfigImpl().set(file);
    }

    public void setEnigmaProfileConfig(Provider<? extends RegularFile> fileProvider) {
        this.getEnigmaProfileConfigImpl().set(fileProvider);
    }
}
