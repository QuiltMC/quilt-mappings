package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Function;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;
import quilt.internal.util.JsonUtils;

public class DownloadMinecraftJarsTask extends MappingsTask {
    public DownloadMinecraftJarsTask() {
        super(Constants.Groups.SETUP_GROUP);
        this.dependsOn("downloadWantedVersionManifest");
        getInputs().files(fileConstants.versionFile);

        getOutputs().files(fileConstants.clientJar, fileConstants.serverJar);

        getOutputs().upToDateWhen(_input -> {
            try {
                Map<String, ?> downloads = JsonUtils.getFromTree(FileUtils.readFileToString(fileConstants.versionFile, Charset.defaultCharset()), "downloads");
                Function<String, String> sha1Getter = name -> JsonUtils.getFromTree(downloads, name, "sha1");
                return fileConstants.clientJar.exists() && fileConstants.serverJar.exists()
                        && validateChecksum(fileConstants.clientJar, sha1Getter.apply("client"))
                        && validateChecksum(fileConstants.serverJar, sha1Getter.apply("server"));
            } catch (Exception e) {
                return false;
            }
        });
    }

    @TaskAction
    public void downloadMinecraftJars() throws IOException {
        if (!fileConstants.versionFile.exists()) {
            throw new RuntimeException("Can't download the jars without the " + fileConstants.versionFile.getName() + " file!");
        }

        //reload in case it changed
        Map<String, ?> version = JsonUtils.getFromTree(FileUtils.readFileToString(fileConstants.versionFile, Charset.defaultCharset()));

        Function<String, String> urlGetter = name -> JsonUtils.getFromTree(version, "downloads", name, "url");

        getLogger().lifecycle(":downloading minecraft jars");

        startDownload()
                .src(urlGetter.apply("client"))
                .dest(fileConstants.clientJar)
                .overwrite(false)
                .download();

        startDownload()
                .src(urlGetter.apply("server"))
                .dest(fileConstants.serverJar)
                .overwrite(false)
                .download();
    }

    @SuppressWarnings("deprecation")
    private static boolean validateChecksum(File file, String checksum) throws IOException {
        if (file != null) {
            HashCode hash = Files.asByteSource(file).hash(Hashing.sha1());
            StringBuilder builder = new StringBuilder();
            for (byte b : hash.asBytes()) {
                builder.append(Integer.toString((b & 0xFF) + 0x100, 16).substring(1));
            }
            return builder.toString().equals(checksum);
        }
        return false;
    }
}
