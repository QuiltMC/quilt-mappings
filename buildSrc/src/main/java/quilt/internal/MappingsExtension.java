package quilt.internal;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.quiltmc.enigma.api.EnigmaProfile;

import java.io.File;
import java.io.IOException;

public abstract class MappingsExtension {
    // TODO see if this can use a BuildService
    /**
     * {@link MappingsPlugin} configures all
     * {@link quilt.internal.tasks.EnigmaProfileConsumingTask EnigmaProfileConsumingTask}s to use this profile.
     */
    public final Provider<EnigmaProfile> enigmaProfile;

    public abstract RegularFileProperty getUnpick();

    private final FileConstants fileConstants;

    public static MappingsExtension get(Project project) {
        return project.getExtensions().getByType(MappingsExtension.class);
    }

    protected abstract RegularFileProperty getEnigmaProfileFile();

    public MappingsExtension(Project project) {
        this.fileConstants = new FileConstants(project);

        this.enigmaProfile = this.getEnigmaProfileFile()
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

    public void setEnigmaProfile(File file) {
        this.getEnigmaProfileFile().set(file);
    }

    public void setEnigmaProfile(RegularFile file) {
        this.getEnigmaProfileFile().set(file);
    }

    public void setEnigmaProfile(Provider<? extends RegularFile> fileProvider) {
        this.getEnigmaProfileFile().set(fileProvider);
    }
}
