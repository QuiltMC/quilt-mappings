package quilt.internal.task.mappings;

import org.gradle.api.tasks.TaskContainer;
import quilt.internal.plugin.EnigmaMappingsPlugin;
import quilt.internal.plugin.QuiltMappingsBasePlugin;

/**
 * Launches the {@linkplain org.quiltmc.enigma.gui.Main Enigma GUI}.
 *
 * @see QuiltMappingsBasePlugin QuiltMappingsBasePlugin's configureEach
 * @see EnigmaMappingsPlugin EnigmaMappingsPlugin's configureEach
 */
public abstract class EnigmaMappingsTask extends AbstractEnigmaMappingsTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link EnigmaMappingsPlugin}.
     */
    public static final String MAPPINGS_TASK_NAME = "mappings";
    /**
     * {@linkplain TaskContainer#register Registered} by {@link EnigmaMappingsPlugin}.
     */
    public static final String MAPPINGS_UNPICKED_TASK_NAME = "mappingsUnpicked";

    public EnigmaMappingsTask() {
        this.getMainClass().set(org.quiltmc.enigma.gui.Main.class.getName());
        this.getMainClass().finalizeValue();
    }
}
