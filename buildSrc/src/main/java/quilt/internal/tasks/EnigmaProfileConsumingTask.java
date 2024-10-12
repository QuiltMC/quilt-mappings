package quilt.internal.tasks;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.quiltmc.enigma.api.EnigmaProfile;
import quilt.internal.QuiltMappingsExtension;
import quilt.internal.QuiltMappingsPlugin;

/**
 * A task that takes an {@link EnigmaProfile} as input.
 * <p>
 * If {@link QuiltMappingsPlugin MappingsPlugin} is applied:
 * <ul>
 *     <li>
 *     {@link #getEnigmaProfile() enigmaProfile} will default to
 *     {@link quilt.internal.QuiltMappingsExtension MappingsExtension}'s
 *     {@link quilt.internal.QuiltMappingsExtension#enigmaProfile enigmaProfile}
 *     <li>
 *     {@link #getEnigmaProfileConfig() enigmaProfileConfig} will default to
 *     {@link quilt.internal.QuiltMappingsExtension MappingsExtension}'s
 *     {@link QuiltMappingsExtension#getEnigmaProfileConfig() enigmaProfileConfig}
 *     <li>
 *     {@link #getSimpleTypeFieldNamesFiles() simpleTypeFieldNamesFiles} will populate based on
 *     {@link #getEnigmaProfile() enigmaProfile}
 * </ul>
 */
public interface EnigmaProfileConsumingTask extends MappingsTask {
    @Internal(
        "An EnigmaProfile cannot be fingerprinted. " +
            "Up-to-date-ness is ensured by getSimpleTypeFieldNamesFiles and its source, " +
            "MappingsExtension::getEnigmaProfileFile."
    )
    Property<EnigmaProfile> getEnigmaProfile();

    /**
     * Don't parse this to create an {@link EnigmaProfile}, use {@link #getEnigmaProfile() enigmaProfile} instead.
     * <p>
     * This is exposed so it can be passed to external processes.
     */
    @InputFile
    RegularFileProperty getEnigmaProfileConfig();

    /**
     * Holds any {@code simple_type_field_names} configuration files obtained from the
     * {@link #getEnigmaProfile() EnigmaProfile}.
     * <p>
     * {@link EnigmaProfileConsumingTask}s may not access these files directly, but they affect enigma's behavior,
     * so they must be considered for up-to-date checks.
     */
    @InputFiles
    ConfigurableFileCollection getSimpleTypeFieldNamesFiles();
}
