package quilt.internal.tasks.build;

import java.util.Map;

import org.gradle.api.GradleException;
import org.gradle.api.artifacts.VersionConstraint;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFile;
import org.gradle.jvm.tasks.Jar;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;

/**
 * TODO is this an accurate description?<br>
 * A task that creates a jar file with Quilt's v2 mapping format.
 * <p>
 * If {@link quilt.internal.QuiltMappingsPlugin QuiltMappingsPlugin} is applied:
 * <ul>
 *     <li>
 *     {@link #getUnpickMeta() unpickMeta} will default to
 *     {@link quilt.internal.QuiltMappingsExtension QuiltMappingsExtension}'s
 *     {@link quilt.internal.QuiltMappingsExtension#getUnpickMeta() unpickMeta}
 *     <li>
 *     {@link #getUnpickDefinition() unpickDefinition} will default to
 *     {@value quilt.internal.tasks.unpick.CombineUnpickDefinitionsTask#TASK_NAME}'s
 *     {@link quilt.internal.tasks.unpick.CombineUnpickDefinitionsTask#getOutput() output}
 *     <li>
 *     {@link #getDestinationDirectory() destinationDirectory} will default to
 *     {@code libs/} inside the project build directory
 * </ul>
 *
 */
public abstract class MappingsV2JarTask extends Jar implements MappingsTask {
    public static final String JAR_UNPICK_META_PATH = "extras/unpick.json";
    public static final String JAR_UNPICK_DEFINITION_PATH = "extras/definitions.unpick";
    public static final String JAR_MAPPINGS_PATH = "mappings/mappings.tiny";

    @InputFile
    public abstract RegularFileProperty getUnpickMeta();

    @InputFile
    public abstract RegularFileProperty getUnpickDefinition();

    @InputFile
    public abstract RegularFileProperty getMappings();

    public MappingsV2JarTask() {
        this.setGroup(Constants.Groups.BUILD_MAPPINGS_GROUP);
        // TODO why?
        this.outputsNeverUpToDate();

        final String version = this.libs().findVersion("unpick")
            .map(VersionConstraint::getRequiredVersion)
            // provide an informative error message if no version is specified
            .orElseThrow(() -> new GradleException(
                """
                Could not find unpick version.
                \tIn order to use any MappingsV2JarTask, an "unpick" version must be specified in the 'libs' \
                version catalog (usually by adding it to 'gradle/libs.versions.toml').\
                """
            ));

        final Provider<RegularFile> unpickMeta = this.getUnpickMeta();

        this.from(unpickMeta, copySpec -> {
            copySpec.expand(Map.of("version", version));

            copySpec.rename(unused -> JAR_UNPICK_META_PATH);
        });

        this.from(this.getUnpickDefinition(), copySpec -> copySpec.rename(unused -> JAR_UNPICK_DEFINITION_PATH));

        this.from(this.getMappings(), copySpec -> copySpec.rename(unused -> JAR_MAPPINGS_PATH));
    }
}
