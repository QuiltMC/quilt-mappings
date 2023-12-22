package quilt.internal.tasks.lint;

import java.util.Locale;
import java.util.function.Function;

import org.quiltmc.enigma.api.translation.mapping.EntryMapping;
import org.quiltmc.enigma.api.translation.representation.AccessFlags;
import org.quiltmc.enigma.api.translation.representation.TypeDescriptor;
import org.quiltmc.enigma.api.translation.representation.entry.Entry;
import org.quiltmc.enigma.api.translation.representation.entry.FieldEntry;

public final class FieldNamingChecker implements Checker<FieldEntry> {
    @Override
    public void check(FieldEntry entry, EntryMapping mapping, Function<Entry<?>, AccessFlags> accessProvider, ErrorReporter errorReporter) {
        if (mapping.targetName() == null) {
            return;
        }

        AccessFlags access = accessProvider.apply(entry);

        if (access == null) {
            throw new RuntimeException("Invalid mappings detected. Please run './gradlew dropInvalidMappings'.");
        }

        TypeDescriptor descriptor = entry.getDesc();
        boolean isAtomic = descriptor.isType() && descriptor.getTypeEntry().getFullName().toLowerCase(Locale.ROOT).contains("atomic");
        if (isAtomic) {
            if (startsWithUppercase(mapping.targetName())) {
                errorReporter.error("atomic field starts with uppercase character '" + mapping.targetName().charAt(0) + "'");
            }
        } else if (access.isStatic() && access.isFinal()) {
            if (!isConstantCase(mapping.targetName())) {
                if (entry.getDesc().isArray()) {
                    errorReporter.error("static final array field is not in CONSTANT_CASE");
                } else {
                    errorReporter.error("static final field is not in CONSTANT_CASE");
                }
            }
        } else {
            if (startsWithUppercase(mapping.targetName())) {
                String error = access.isFinal() ? "non-static" : "non-final";
                errorReporter.error(error + " field starts with uppercase character '" + mapping.targetName().charAt(0) + "'");
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
