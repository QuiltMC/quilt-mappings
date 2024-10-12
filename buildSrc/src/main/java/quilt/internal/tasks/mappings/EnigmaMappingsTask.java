package quilt.internal.tasks.mappings;

public abstract class EnigmaMappingsTask extends AbstractEnigmaMappingsTask {
    public EnigmaMappingsTask() {
        // this configuration can stay here because it's what makes this an EnigmaMappingsTask
        this.getMainClass().set(org.quiltmc.enigma.gui.Main.class.getName());
        this.getMainClass().finalizeValue();
    }
}
