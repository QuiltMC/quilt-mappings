package quilt.internal.decompile;

import org.gradle.api.logging.Logger;
import quilt.internal.decompile.vineflower.VineflowerDecompiler;

public enum Decompilers {
    VINEFLOWER(VineflowerDecompiler::new);

    private final Factory factory;

    Decompilers(Factory factory) {
        this.factory = factory;
    }

    public AbstractDecompiler create(Logger logger) {
        return this.factory.create(logger);
    }

    private interface Factory {
        AbstractDecompiler create(Logger logger);
    }
}
