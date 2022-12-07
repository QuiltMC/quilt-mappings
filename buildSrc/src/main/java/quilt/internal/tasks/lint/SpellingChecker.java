package quilt.internal.tasks.lint;

import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.representation.AccessFlags;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpellingChecker implements Checker<Entry<?>> {
    private static final String PACKAGE_INFO_CLASS_PACKAGE = "net/minecraft/unused/packageinfo/";
    private static final String METHOD_PREFIX = "m_";
    private static final String CLASS_PREFIX = "C_";
    private static final char PACKAGE_SEPARATOR = '/';

    private static final Set<String> ALLOWED_WORDS = new HashSet<>();
    static {
        // collect allowed words
        URL url;
        try {
            url = new URL("https://raw.githubusercontent.com/ix0rai/qm-base-allowed-wordlist/main/allowed_english_words.txt");
            Set<String> englishWords = getLines(url.openStream());
            ALLOWED_WORDS.addAll(englishWords);
        } catch (Exception e) {
            throw new RuntimeException("failed to download and read english word file for spell check in mapping lint task!", e);
        }

        // collect minecraft words
        InputStream parsedFile = SpellingChecker.class.getClassLoader().getResourceAsStream("minecraft_specific_words.txt");
        if (parsedFile == null) {
            throw new RuntimeException("could not find minecraft word file for spelling check in mapping lint task!");
        }

        Set<String> minecraftWords = getLines(parsedFile);
        ALLOWED_WORDS.addAll(minecraftWords);
    }

    private static Set<String> getLines(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream)).lines().filter(line -> !line.isBlank() && !line.startsWith("//")).collect(Collectors.toSet());
    }

    @Override
    public void check(Entry<?> entry, EntryMapping mapping, Function<Entry<?>, AccessFlags> accessProvider, ErrorReporter errorReporter) {
        String name = mapping.targetName();

        if (name != null
                && !name.startsWith(PACKAGE_INFO_CLASS_PACKAGE)
                // ignore unmapped methods and classes
                && !(entry instanceof MethodEntry && name.startsWith(METHOD_PREFIX))
                && !(entry instanceof ClassEntry && name.startsWith(CLASS_PREFIX))) {
            List<String> namesToSplit = new ArrayList<>();

            // a contains check is necessary here because inner classes do not have package data
            if (entry instanceof ClassEntry && name.contains(String.valueOf(PACKAGE_SEPARATOR))) {
                // add class name - we don't have to handle underscores
                namesToSplit.add(name.substring(name.lastIndexOf(PACKAGE_SEPARATOR) + 1));
                // add package names and handle underscores
                String[] packageNames = name.substring(0, name.lastIndexOf(PACKAGE_SEPARATOR)).split(String.valueOf(PACKAGE_SEPARATOR));
                for (String packageName : packageNames) {
                    String[] split = packageName.split("_");
                    namesToSplit.addAll(List.of(split));
                }
            } else {
                // handle underscores
                String[] split = name.split("_");
                namesToSplit.addAll(List.of(split));
            }

            // split by uppercase characters and numbers and preserve
            List<String> splitNames = new ArrayList<>();
            for (String nameToSplit : namesToSplit) {
                // split by uppercase letters and preserve them in the split strings
                // also map to all lowercase
                List<String> split = Arrays.stream(nameToSplit.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")).map(String::toLowerCase).toList();
                splitNames.addAll(split);
            }

            for (String word : splitNames) {
                // ignore numbers
                // this requires special handling since we can't use the same regex we use for uppercase characters
                // that way it would preserve the numbers, and we'd have to strip them anyway
                boolean containsInts = false;
                for (char c : word.toCharArray()) {
                    if (Character.isDigit(c)) {
                        containsInts = true;
                        break;
                    }
                }

                if (containsInts) {
                    String[] splitWords = word.split("\\d");
                    for (String splitWord : splitWords) {
                        checkWord(splitWord, errorReporter);
                    }
                } else {
                    checkWord(word, errorReporter);
                }
            }
        }
    }

    private void checkWord(String word, ErrorReporter reporter) {
        if (!word.isEmpty() && !ALLOWED_WORDS.contains(word)) {
            reporter.error("entry name contains unknown/misspelled word: " + word);
        }
    }
}
