package quilt.internal.tasks.setup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Function;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import de.undercouch.gradle.tasks.download.DownloadAction;
import groovy.json.JsonSlurper;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import quilt.internal.Constants;
import quilt.internal.FileConstants;
import quilt.internal.MappingsPlugin;

public class DownloadMinecraftJarsTask extends DefaultTask {
    public DownloadMinecraftJarsTask() {
        this.setGroup(Constants.Groups.SETUP_GROUP);
        this.dependsOn("downloadWantedVersionManifest");
        FileConstants fileConstants = MappingsPlugin.getExtension(getProject()).getFileConstants();
        getInputs().files(fileConstants.versionFile);

        getOutputs().files(fileConstants.clientJar, fileConstants.serverJar);

        getOutputs().upToDateWhen(_input -> {
            try {
                Map<String, ?> downloads = (Map<String, ?>) ((Map<String, ?>) new JsonSlurper().parseText(FileUtils.readFileToString(fileConstants.versionFile, Charset.defaultCharset()))).get("downloads");
                Function<String, String> sha1Getter = name -> ((String) ((Map<String, ?>) downloads.get(name)).get("sha1"));
                return fileConstants.clientJar.exists() && fileConstants.serverJar.exists()
                        && validateChecksum(fileConstants.clientJar, sha1Getter.apply("client"))
                        && validateChecksum(fileConstants.serverJar, sha1Getter.apply("server"));
            } catch (Exception e) {
                return false;
            }
        });

        doLast(_this -> {
            if (!fileConstants.versionFile.exists()) {
                throw new RuntimeException("Can't download the jars without the ${versionFile.name} file!");
            }

            //reload in case it changed
            try {
                Map<String, ?> version = (Map<String, ?>) new JsonSlurper().parseText(FileUtils.readFileToString(fileConstants.versionFile, Charset.defaultCharset()));


                Function<String, String> urlGetter = name -> ((String) ((Map<String, ?>) ((Map<String, ?>) version.get("downloads")).get(name)).get("url"));

                getLogger().lifecycle(":downloading minecraft jars");

                DownloadAction downloadClient = new DownloadAction(getProject(), this);
                downloadClient.src(new URL(urlGetter.apply("client")));
                downloadClient.dest(fileConstants.clientJar);
                downloadClient.overwrite(false);

                DownloadAction downloadServer = new DownloadAction(getProject(), this);
                downloadServer.src(new URL(urlGetter.apply("server")));
                downloadServer.dest(fileConstants.serverJar);
                downloadServer.overwrite(false);

                downloadClient.execute();
                downloadServer.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static boolean validateChecksum(File file, String checksum) throws IOException {
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
