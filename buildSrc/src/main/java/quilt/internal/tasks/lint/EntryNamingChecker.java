package quilt.internal.tasks.lint;

import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.representation.AccessFlags;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.FieldEntry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;

import java.util.function.Function;

public class EntryNamingChecker implements Checker<Entry<?>> {
    @Override
    public void check(Entry<?> entry, EntryMapping mapping, Function<Entry<?>, AccessFlags> accessProvider, ErrorReporter errorReporter) {
        if (mapping.targetName() == null) {
            return;
        }

        if (entry instanceof FieldEntry) {
            // Fields have their own naming checker
            return;
        }

        String name = mapping.targetName();
        AccessFlags access = accessProvider.apply(entry);

        if (access == null) {
            throw new RuntimeException("Invalid mappings detected. Please run './gradlew dropInvalidMappings'.");
        }

        if (entry instanceof ClassEntry) {
            if (!startsWithUppercase(name)) {
                errorReporter.error("class name does not start with uppercase character");
            }
        } else {
            if (startsWithUppercase(name)) {
                errorReporter.error("non-class entry name starts with uppercase character");
            } else if (entry instanceof MethodEntry) {
                if (entry.getName().equals("<init>") || entry.getName().equals("<clinit>")) {
                    errorReporter.error("constructor method has a name");
                } else if (access.isSynthetic()) {
                    errorReporter.warning("synthetic method has a name");
                }
            }
        }

    }

    private static boolean startsWithUppercase(String s) {
        return Character.isUpperCase(s.charAt(0));
    }
}
