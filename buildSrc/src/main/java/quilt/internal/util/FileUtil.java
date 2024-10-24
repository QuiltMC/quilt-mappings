package quilt.internal.util;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.file.RegularFile;

public final class FileUtil {
    private FileUtil() { }

    public static String getNameWithExtension(RegularFile file, String extensionReplacement) {
        return replaceExtension(file.getAsFile().getName(), extensionReplacement);
    }

    public static String replaceExtension(String fileName, String replacement) {
        final String newName = FilenameUtils.removeExtension(fileName) + replacement;

        if (newName.equals(fileName)) {
            throw new IllegalArgumentException(
                """
                File extension replacement did not change name.
                \tfileName: "%s"
                \treplacement: "%s"
                """.formatted(fileName, replacement)
            );
        }

        return newName;
    }
}
