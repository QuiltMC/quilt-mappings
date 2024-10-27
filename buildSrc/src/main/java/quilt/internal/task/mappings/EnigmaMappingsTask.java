package quilt.internal.task.mappings;

import quilt.internal.plugin.QuiltMappingsBasePlugin;

/**
 * Launches the {@linkplain org.quiltmc.enigma.gui.Main Enigma GUI}.
 *
 * @see QuiltMappingsBasePlugin QuiltMappingsBasePlugin's configureEach
 */
public abstract class EnigmaMappingsTask extends AbstractEnigmaMappingsTask {
    public static final String MAPPINGS_TASK_NAME = "mappings";
    public static final String MAPPINGS_UNPICKED_TASK_NAME = "mappingsUnpicked";

    public EnigmaMappingsTask() {
        this.getMainClass().set(org.quiltmc.enigma.gui.Main.class.getName());
        this.getMainClass().finalizeValue();
    }
}
