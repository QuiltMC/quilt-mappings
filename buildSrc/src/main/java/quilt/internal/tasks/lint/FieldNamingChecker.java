package quilt.internal.tasks.lint;

import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.representation.AccessFlags;
import cuchaz.enigma.translation.representation.TypeDescriptor;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.FieldEntry;

import java.util.Locale;
import java.util.function.Function;

public final class FieldNamingChecker implements Checker<FieldEntry> {
    @Override
    public void check(FieldEntry entry, EntryMapping mapping, Function<Entry<?>, AccessFlags> accessProvider, ErrorReporter errorReporter) {
        if (mapping.targetName() == null) {
            return;
        }

        AccessFlags access = accessProvider.apply(entry);

        TypeDescriptor descriptor = entry.getDesc();
        boolean isAtomic = descriptor.isType() && descriptor.getTypeEntry().getFullName().toLowerCase(Locale.ROOT).contains("atomic");
        if (isAtomic) {
            if (startsWithUppercase(mapping.targetName())) {
                errorReporter.warning("atomic field starts with uppercase character '" + mapping.targetName().charAt(0) + "'");
            }
        } else if (access.isStatic() && access.isFinal()) {
            if (!isConstantCase(mapping.targetName())) {
                errorReporter.error("static final field is not in CONSTANT_CASE");
            }
        } else {
            if (startsWithUppercase(mapping.targetName())) {
                errorReporter.warning("non-static or non-final field starts with uppercase character '" + mapping.targetName().charAt(0) + "'");
            }
        }
    }

    private static boolean isConstantCase(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isLowerCase(c)) {
                return false;
            }
        }

        return true;
    }

    private static boolean startsWithUppercase(String s) {
        return Character.isUpperCase(s.charAt(0));
    }
}
