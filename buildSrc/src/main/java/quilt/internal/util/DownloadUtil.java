package quilt.internal.util;

import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public final class DownloadUtil {
    private DownloadUtil() { }

    /**
     * Similar to {@link #downloadUnhandled(String, File, boolean, Logger)} except that exceptions are caught and
     * re-thrown as {@link GradleException}s so callers needn't catch them.
     */
    public static boolean download(String url, File dest, boolean overwrite, @Nullable Logger logger) {
        try {
            return downloadUnhandled(urlOf(url), dest, overwrite, logger);
        } catch (IOException e) {
            throw new GradleException(
                """
                Failed to download:
                \tfrom: %s
                \tto: %s
                """.formatted(url, dest),
                e
            );
        }
    }

    /**
     * @see #urlOfUnhandled(String)
     * @see #downloadUnhandled(URL, File, boolean, Logger)
     */
    public static boolean downloadUnhandled(String url, File dest, boolean overwrite, @Nullable Logger logger) throws
            IOException, URISyntaxException {
        return downloadUnhandled(urlOfUnhandled(url), dest, overwrite, logger);
    }

    /**
     * Downloads a {@link File} from the passed {@code url} and saves it to the passed {@code dest}.
     * <p>
     * Uses {@link FileUtils#copyURLToFile} to download the file, see its javadoc for more details.
     *
     * @param url where to download the file from
     * @param dest where the downloaded file will be saved to
     * @param overwrite if {@code false}, the download will be skipped if {@code dest} already
     * {@link File#exists() exists}
     * @param logger if not {@code null}, a message will be logged before starting to download;
     *              nothing is logged if the download is skipped
     *
     * @return {@code true} if the file is downloaded, or {@code false} otherwise
     *
     * @throws IOException see {@link FileUtils#copyURLToFile}
     */
    public static boolean downloadUnhandled(URL url, File dest, boolean overwrite, @Nullable Logger logger) throws
            IOException {
        if (overwrite || !dest.exists()) {
            if (logger != null) {
                logger.lifecycle("Downloading {}", url);
            }

            // TODO I just made these timeouts up, they could probably be better
            FileUtils.copyURLToFile(url, dest, 10_000, 30_000);

            return true;
        } else {
            return false;
        }
    }

    /**
     * Similar to {@link #urlOfUnhandled(String)} except that exceptions are caught and
     * re-thrown as {@link GradleException}s so callers needn't catch them.
     */
    public static URL urlOf(String url) {
        try {
            return urlOfUnhandled(url);
        } catch (URISyntaxException | MalformedURLException e) {
            throw new GradleException("Invalid url: " + url, e);
        }
    }

    /**
     * @return the {@link URL} represented by the passed {@code url}
     * @throws MalformedURLException if the passed {@code url} is malformed
     * @throws URISyntaxException if the passed {@code url} uses invalid syntax
     */
    public static URL urlOfUnhandled(String url) throws MalformedURLException, URISyntaxException {
        return new URI(url).toURL();
    }
}
