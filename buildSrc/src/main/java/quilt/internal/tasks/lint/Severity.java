package quilt.internal.tasks.lint;

/**
 * The severity of a lint error.
 */
public enum Severity {
    /**
     * A severe error that should fail the lint task.
     */
    ERROR,

    /**
     * A soft warning that does not fail the lint task.
     */
    WARNING
}
