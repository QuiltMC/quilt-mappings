package quilt.internal.decompile;

import quilt.internal.decompile.cfr.CfrDecompiler;
import quilt.internal.decompile.quiltflower.QuiltflowerDecompiler;

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
