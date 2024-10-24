package quilt.internal.tasks;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.quiltmc.enigma.api.EnigmaProfile;
import quilt.internal.QuiltMappingsExtension;
import quilt.internal.util.EnigmaProfileService;

/**
 * A task that takes an {@link EnigmaProfile} as input.
 * <p>
 * {@link quilt.internal.plugin.QuiltMappingsBasePlugin QuiltMappingsBasePlugin}
 * {@linkplain org.gradle.api.tasks.TaskCollection#configureEach configures} the following defaults:
 * <ul>
 *     <li>
 *     {@link #getEnigmaProfileService() enigmaProfileService}: the
 *     {@value quilt.internal.util.EnigmaProfileService#ENIGMA_PROFILE_SERVICE_NAME} service
 *     which reads {@link quilt.internal.QuiltMappingsExtension MappingsExtension}'s
 *     {@link QuiltMappingsExtension#getEnigmaProfileConfig() enigmaProfileConfig}
 *     <li>
 *     {@link #getEnigmaProfileConfig() enigmaProfileConfig}:
 *     {@link quilt.internal.QuiltMappingsExtension MappingsExtension}'s
 *     {@link QuiltMappingsExtension#getEnigmaProfileConfig() enigmaProfileConfig}
 *     <li>
 *     {@link #getSimpleTypeFieldNamesFiles() simpleTypeFieldNamesFiles}: the
 *     {@value quilt.internal.util.EnigmaProfileService#ENIGMA_PROFILE_SERVICE_NAME} service's
 *     {@link EnigmaProfileService#getProfile() profile}'s
 *     {@value org.quiltmc.enigma_plugin.Arguments#SIMPLE_TYPE_FIELD_NAMES_PATH}s
 * </ul>
 */
public interface EnigmaProfileConsumingTask extends MappingsTask {
    @Internal("@ServiceReference is @Incubating")
    Property<EnigmaProfileService> getEnigmaProfileService();

    /**
     * Don't parse this to create an {@link EnigmaProfile}, use the one provided by
     * {@link #getEnigmaProfileService() enigmaProfileService} instead.
     * <p>
     * This is exposed so it can be passed to external processes.
     */
    @InputFile
    RegularFileProperty getEnigmaProfileConfig();

    /**
     * Holds any {@value org.quiltmc.enigma_plugin.Arguments#SIMPLE_TYPE_FIELD_NAMES_PATH}s
     * configuration files obtained from {@link #getEnigmaProfileService() enigmaProfileService}'s
     * {@link EnigmaProfileService#getProfile() profile}.
     * <p>
     * {@link EnigmaProfileConsumingTask}s may not access these files directly, but they affect Enigma's behavior,
     * so they must be considered for up-to-date checks.
     */
    @InputFiles
    ConfigurableFileCollection getSimpleTypeFieldNamesFiles();
}
