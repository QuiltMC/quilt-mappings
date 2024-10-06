package quilt.internal.tasks;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.quiltmc.enigma.api.EnigmaProfile;
import org.quiltmc.enigma.api.service.JarIndexerService;
import quilt.internal.QuiltMappingsExtension;
import quilt.internal.QuiltMappingsPlugin;

import java.util.Collection;
import java.util.stream.Stream;

import static org.quiltmc.enigma_plugin.Arguments.SIMPLE_TYPE_FIELD_NAMES_PATH;

/**
 * A task that takes an {@link EnigmaProfile} as input.
 * <p>
 * If {@link QuiltMappingsPlugin MappingsPlugin} is applied, any {@code EnigmaProfileConsumingTask}s will use
 * {@link QuiltMappingsExtension MappingsExtension}'s
 * {@link QuiltMappingsExtension#enigmaProfile enigmaProfile} by default.
 */
public abstract class EnigmaProfileConsumingTask extends DefaultMappingsTask {
    @Internal(
        "An EnigmaProfile cannot be fingerprinted. " +
            "Up-to-date-ness is ensured by getSimpleTypeFieldNamesFiles and its source, " +
            "MappingsExtension::getEnigmaProfileFile."
    )
    public abstract Property<EnigmaProfile> getEnigmaProfile();

    /**
     * Holds any {@code simple_type_field_names} configuration files obtained from the
     * {@link #getEnigmaProfile() EnigmaProfile}.
     * <p>
     * {@link EnigmaProfileConsumingTask}s may not access these files directly, but they affect enigma's behavior,
     * so they must be considered for up-to-date checks.
     */
    @InputFiles
    protected abstract Property<FileCollection> getSimpleTypeFieldNamesFiles();

    public EnigmaProfileConsumingTask(String group) {
        super(group);

        final Project project = this.getProject();

        this.getSimpleTypeFieldNamesFiles().set(
            project.provider(() -> project.files(
                this.getEnigmaProfile().get().getServiceProfiles(JarIndexerService.TYPE).stream()
                    .flatMap(service -> service.getArgument(SIMPLE_TYPE_FIELD_NAMES_PATH).stream())
                    .map(stringOrStrings -> stringOrStrings.mapBoth(Stream::of, Collection::stream))
                    .flatMap(bothStringStreams -> bothStringStreams.left().orElseGet(bothStringStreams::rightOrThrow))
                    .toList()
            )
        ));
    }
}
