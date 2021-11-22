package quilt.internal.tasks.lint;

import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.representation.AccessFlags;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;

import java.util.function.Function;
import java.util.regex.Pattern;

public final class JavadocChecker implements Checker<Entry<?>> {
    private static final Pattern PARAM_DOC_LINE = Pattern.compile("^@param\\s+[^<].*$");

    @Override
    public void check(Entry<?> entry, EntryMapping mapping, Function<Entry<?>, AccessFlags> accessProvider, ErrorReporter errorReporter) {
        String javadoc = mapping.javadoc();

        if (javadoc != null && !javadoc.isEmpty()) {
            if (entry instanceof LocalVariableEntry lv && lv.isArgument()) {
                if (javadoc.endsWith(".")) {
                    errorReporter.error("parameter javadoc ends with '.'");
                }

                if (Character.isUpperCase(javadoc.charAt(0))) {
                    String word = getFirstWord(javadoc);

                    // ignore single-letter "words" (like X or Z)
                    if (word.length() > 1) {
                        errorReporter.error("parameter javadoc starts with uppercase word '" + word + "'");
                    }
                }
            } else if (entry instanceof MethodEntry) {
                if (javadoc.lines().anyMatch(JavadocChecker::isRegularMethodParameter)) {
                    errorReporter.error("method javadoc contains parameter docs, which should be on the parameter itself");
                }
            }
        }
    }

    private static boolean isRegularMethodParameter(String line) {
        return PARAM_DOC_LINE.matcher(line).matches();
    }

    private static String getFirstWord(String str) {
        int i = str.indexOf(' ');
        return i != -1 ? str.substring(0, i) : str;
    }
}
