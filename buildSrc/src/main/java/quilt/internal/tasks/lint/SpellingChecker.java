package quilt.internal.tasks.lint;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.representation.AccessFlags;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;

public class SpellingChecker implements Checker<Entry<?>> {
    private static final String PACKAGE_INFO_CLASS_PACKAGE = "net/minecraft/unused/packageinfo/";
    private static final String METHOD_PREFIX = "m_";
    private static final String CLASS_PREFIX = "C_";
    private static final char PACKAGE_SEPARATOR = '/';

    private static final Set<String> MINECRAFT_WORDS = new HashSet<>();
    static {
        // collect minecraft words
        InputStream parsedFile = SpellingChecker.class.getClassLoader().getResourceAsStream("minecraft_specific_words.txt");
        if (parsedFile == null) {
            throw new RuntimeException("could not find minecraft word file for spelling check in mapping lint task!");
        }

        Set<String> minecraftWords = getLines(parsedFile);
        MINECRAFT_WORDS.addAll(minecraftWords);
    }

    private static Set<String> getLines(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream)).lines().filter(line -> !line.isBlank() && !line.startsWith("//")).collect(Collectors.toSet());
    }

    private static final Set<String> words = new HashSet<>();

    @Override
    public void update(MappingLintTask.LintParameters parameters) {
        words.addAll(MINECRAFT_WORDS);

        try {
            FileInputStream spellingFile = new FileInputStream(parameters.getSpellingFile().get().getAsFile());
            Set<String> spellingWords = getLines(spellingFile);
            words.addAll(spellingWords);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Unable to find spelling file!");
        }
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
                boolean containsInts = word.chars().anyMatch(Character::isDigit);

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
        if (!word.isEmpty() && !words.contains(word)) {
            // ignore plural versions of words
            if (word.endsWith("s") && checkPlural(word)) {
                return;
            }

            reporter.error("entry name contains unknown/misspelled word: " + word);
        }
    }

    private boolean checkPlural(String word) {
        String nonPlural;
        if (word.endsWith("ies")) { // berries -> berry
            nonPlural = word.substring(word.length() - 3) + "y";
        } else if (word.endsWith("ves")) { // leaves -> leaf
            nonPlural = word.substring(word.length() - 3) + "f";
            if (!words.contains(nonPlural)) { // knives -> knife
                nonPlural = word.substring(word.length() - 3) + "fe";
            } else {
                // word has been found, we can skip the next contains check
                return true;
            }
        } else if (word.endsWith("ses")) { // glasses -> glass
            nonPlural = word.substring(word.length() - 2);
        } else { // ones -> one
            nonPlural = word.substring(word.length() - 1);
        }

        return words.contains(nonPlural);
    }
}
