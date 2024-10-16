package quilt.internal.tasks.build;

import java.util.Map;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.jvm.tasks.Jar;
import org.gradle.work.DisableCachingByDefault;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;

import javax.inject.Inject;

/**
 * TODO is this an accurate description?<br>
 * A task that creates a jar file with Quilt's v2 mapping format.
 * <p>
 * {@link quilt.internal.QuiltMappingsPlugin QuiltMappingsPlugin} applies the following defaults to all
 * {@code MappingsV2JarTask}s:
 * <ul>
 *     <li>
 *     {@link quilt.internal.QuiltMappingsExtension QuiltMappingsExtension}'s
 *     {@link quilt.internal.QuiltMappingsExtension#getUnpickMeta() unpickMeta}
 *     to {@link #getUnpickMeta() unpickMeta}
 *     <li>
 *     {@value quilt.internal.tasks.unpick.CombineUnpickDefinitionsTask#COMBINE_UNPICK_DEFINITIONS_TASK_NAME}'s
 *     {@link quilt.internal.tasks.unpick.CombineUnpickDefinitionsTask#getOutput() output}
 *     to {@link #getUnpickDefinition() unpickDefinition}
 *     <li>
 *     {@code libs/} inside the
 *     {@linkplain org.gradle.api.file.ProjectLayout#getBuildDirectory() project build directory}
 *     to {@link #getDestinationDirectory() destinationDirectory}
 * </ul>
 */

// TODO why?
@DisableCachingByDefault(because = "unknown")
public abstract class MappingsV2JarTask extends Jar implements MappingsTask {
    public static final String JAR_UNPICK_META_PATH = "extras/unpick.json";
    public static final String JAR_UNPICK_DEFINITION_PATH = "extras/definitions.unpick";
    public static final String JAR_MAPPINGS_PATH = "mappings/mappings.tiny";
    public static final String V_2_UNMERGED_MAPPINGS_JAR_TASK_NAME = "v2UnmergedMappingsJar";
    public static final String V_2_MERGED_MAPPINGS_JAR_TASK_NAME = "v2MergedMappingsJar";

    @InputFile
    public abstract RegularFileProperty getUnpickMeta();

    @InputFile
    public abstract RegularFileProperty getUnpickDefinition();

    @InputFile
    public abstract RegularFileProperty getMappings();

    // unpick version can't be a property because it's used when the task is instantiated
    public final String unpickVersion;

    @Inject
    public MappingsV2JarTask(String unpickVersion) {
        this.setGroup(Constants.Groups.BUILD_MAPPINGS);

        this.unpickVersion = unpickVersion;

        this.from(this.getUnpickMeta(), copySpec -> {
            copySpec.expand(Map.of("version", this.unpickVersion));

            copySpec.rename(unused -> JAR_UNPICK_META_PATH);
        });

        this.from(this.getUnpickDefinition(), copySpec -> copySpec.rename(unused -> JAR_UNPICK_DEFINITION_PATH));

        this.from(this.getMappings(), copySpec -> copySpec.rename(unused -> JAR_MAPPINGS_PATH));
    }
}
