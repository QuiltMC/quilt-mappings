package quilt.internal.tasks.lint;

import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.representation.AccessFlags;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpellingChecker implements Checker<Entry<?>> {
    private static final String PACKAGE_INFO_CLASS_PACKAGE = "net/minecraft/unused/packageinfo/";

    private static final Set<String> ALLOWED_WORDS = new HashSet<>();
    static {
        // collect allowed words
        Set<String> englishWords = getLines("allowed_english_words.txt");
        ALLOWED_WORDS.addAll(englishWords);

        Set<String> minecraftWords = getLines("minecraft_specific_words.txt");
        // ignore comments and empty lines
        Set<String> minecraftWordsCopy = Set.copyOf(minecraftWords);
        for (String word : minecraftWordsCopy) {
            if (word.isEmpty() || word.startsWith("//")) {
                minecraftWords.remove(word);
            }
        }

        ALLOWED_WORDS.addAll(minecraftWords);
    }

    private static Set<String> getLines(String filename) {
        InputStream parsedFile = SpellingChecker.class.getClassLoader().getResourceAsStream(filename);
        if (parsedFile == null) {
            throw new RuntimeException("could not find" + filename + "for spelling check in mapping lint task");
        }

        return new java.util.Scanner(parsedFile).useDelimiter("\\A").next().lines().collect(Collectors.toSet());
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
                    // split by uppercase characters and preserve
                    words.addAll(List.of(Arrays.stream(underscoreSeparatedWord.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")).map(String::toLowerCase).toArray(String[]::new)));
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
