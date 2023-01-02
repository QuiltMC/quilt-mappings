package quilt.internal.input;

import java.util.Collection;

public class JClass<T extends Collection<T>> extends IClass<T> {
    @Override
    public T get() {
        return null;
    }

    @Override
    public void set(T value) {
    }
}
