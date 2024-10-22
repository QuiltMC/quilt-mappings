package quilt.internal.tasks.lint;

import java.util.function.BiConsumer;

/**
 * A {@link BiConsumer} that accepts errors by their severity and message.
 */
@FunctionalInterface
public interface ErrorReporter extends BiConsumer<Severity, String> {
    /**
     * Reports a severe error.
     *
     * @param message the error message
     */
    default void error(String message) {
        this.accept(Severity.ERROR, message);
    }

    /**
     * Reports a warning.
     *
     * @param message the error message
     */
    default void warning(String message) {
        this.accept(Severity.WARNING, message);
    }
}
