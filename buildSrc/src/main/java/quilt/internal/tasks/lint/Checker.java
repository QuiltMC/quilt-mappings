package quilt.internal.tasks.lint;

import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.representation.AccessFlags;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.FieldEntry;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Function;

/**
 * A checker checks mapping entries for formatting/convention errors and warnings.
 *
 * @param <E> the checked entry
 */
@FunctionalInterface
public interface Checker<E extends Entry<?>> extends Serializable {
    /**
     * The default checkers in Filament, following Yarn conventions.
     */
    Set<Checker<Entry<?>>> DEFAULT_CHECKERS = Set.of(
            new JavadocChecker(),
            new EntryNamingChecker(),
            new FieldNamingChecker().withTypeGuard(FieldEntry.class)
    );

    /**
     * Checks an entry and reports any errors/warnings to a reporter.
     *
     * @param entry          the entry to check
     * @param mapping        its mapping
     * @param accessProvider a function that provides the access flags of an entry
     * @param errorReporter  the error reporter
     */
    void check(E entry, EntryMapping mapping, Function<Entry<?>, AccessFlags> accessProvider, ErrorReporter errorReporter);

    /**
     * Creates a new checker with a type guard. The created checker delegates to this checker,
     * but only when the entry is an instance of the specified type.
     *
     * @param entryType the required entry type
     * @return the created checker
     */
    default Checker<Entry<?>> withTypeGuard(Class<E> entryType) {
        return (entry, mapping, access, errorReporter) -> {
            if (entryType.isInstance(entry)) {
                check(entryType.cast(entry), mapping, access, errorReporter);
            }
        };
    }
}
