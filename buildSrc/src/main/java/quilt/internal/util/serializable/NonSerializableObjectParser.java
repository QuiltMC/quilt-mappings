package quilt.internal.util.serializable;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Serializable;

public abstract class NonSerializableObjectParser<N> implements Serializable {
    private final File source;

    @Nullable
    private transient N nonSerializable;

    public NonSerializableObjectParser(File source) {
        this.source = source;
    }

    public final N get() {
        if (this.nonSerializable == null) {
            this.nonSerializable = this.parse(this.source);
        }

        return this.nonSerializable;
    }

    protected abstract N parse(File source);
}
