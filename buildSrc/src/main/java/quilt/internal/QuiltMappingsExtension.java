package quilt.internal;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.quiltmc.enigma.api.EnigmaProfile;

public abstract class QuiltMappingsExtension {
    public static final String EXTENSION_NAME = "quiltMappings";

    public abstract DirectoryProperty getMappingsDir();

    /**
     * Don't parse this to create an {@link EnigmaProfile}, use the
     * {@value quilt.internal.util.EnigmaProfileService#ENIGMA_PROFILE_SERVICE_NAME} service's
     * {@link quilt.internal.util.EnigmaProfileService#getProfile() profile} instead.
     * <p>
     * This is exposed so it can be passed to external processes.
     */
    public abstract RegularFileProperty getEnigmaProfileConfig();

    public abstract RegularFileProperty getUnpickMeta();
}
