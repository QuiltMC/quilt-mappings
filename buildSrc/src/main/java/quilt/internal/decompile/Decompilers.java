package quilt.internal.decompile;

public enum Decompilers {
    CFR(CfrDecompiler::new),
    QUILTFLOWER(QuiltflowerDecompiler::new);

    private final DecompilerProvider provider;

    Decompilers(DecompilerProvider decompilerProvider) {
        this.provider = decompilerProvider;
    }

    public DecompilerProvider getProvider() {
        return this.provider;
    }
}
