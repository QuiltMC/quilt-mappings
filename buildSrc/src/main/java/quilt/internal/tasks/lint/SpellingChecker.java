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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpellingChecker implements Checker<Entry<?>> {
    private static final String PACKAGE_INFO_CLASS_PACKAGE = "net/minecraft/unused/packageinfo/";

    private static final Set<String> ALLOWED_WORDS = new HashSet<>();
    static {
        // collect allowed words
        URL url;
        try {
            url = new URL("https://raw.githubusercontent.com/ix0rai/qm-base-allowed-wordlist/main/allowed_english_words.txt");
            Set<String> englishWords = new java.util.Scanner(url.openStream()).useDelimiter("\\A").next().lines().collect(Collectors.toSet());
            ALLOWED_WORDS.addAll(englishWords);
        } catch (Exception e) {
            throw new RuntimeException("failed to download and read english word file for spell check in mapping lint task!\n" + e);
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
                && !(entry instanceof MethodEntry && name.startsWith("m_"))
                && !(entry instanceof ClassEntry && name.startsWith("C_"))) {
            Set<String> words = new HashSet<>();

            // fixme optimise!
            // split by slashes
            String[] slashSeparatedWords = name.split("/");
            for (String word : slashSeparatedWords) {
                // split by underscores
                String[] underscoreSeparatedWords = word.split("_");
                for (String underscoreSeparatedWord : underscoreSeparatedWords) {
                    // split by uppercase characters and numbers and preserve
                    String[] splitWords = underscoreSeparatedWord.split("(?<!(^|[A-Z0-9]))(?=[A-Z0-9])|(?<!^)(?=[A-Z0-9][a-z])");
                    words.addAll(Arrays.stream(splitWords).map(String::toLowerCase).toList());
                }
            }

            for (String word : words) {
                // ignore numbers
                // fixme this is a terrible way of doing this!
                boolean containsInts = false;
                for (char c : word.toCharArray()) {
                    if (Character.isDigit(c)) {
                        containsInts = true;
                        break;
                    }
                }

                // fixme this also probably a terrible way of doing this!
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
