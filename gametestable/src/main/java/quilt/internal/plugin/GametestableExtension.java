package quilt.internal.plugin;

public abstract class GametestableExtension {
    private final String version;

    public GametestableExtension(String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }
}
