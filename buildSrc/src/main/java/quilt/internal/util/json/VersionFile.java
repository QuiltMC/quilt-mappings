package quilt.internal.util.json;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class VersionFile {
    private MinecraftDownload clientJar, serverJar;
    private List<LibraryDownload> libraries;

    public VersionFile(MinecraftDownload clientJar, MinecraftDownload serverJar, List<LibraryDownload> libraries) {
        this.clientJar = clientJar;
        this.serverJar = serverJar;
        this.libraries = libraries;
    }

    public MinecraftDownload clientJar() {
        return clientJar;
    }

    public MinecraftDownload serverJar() {
        return serverJar;
    }

    public List<LibraryDownload> libraries() {
        return libraries;
    }

    public static VersionFile fromJson(String json) {
        Gson gson = new Gson();
        JsonObject file = JsonParser.parseString(json).getAsJsonObject();
        JsonObject downloads = file.get("downloads").getAsJsonObject();

        MinecraftDownload clientJar = gson.fromJson(downloads.get("client"), MinecraftDownload.class);
        MinecraftDownload serverJar = gson.fromJson(downloads.get("server"), MinecraftDownload.class);

        List<LibraryDownload> libraries = StreamSupport.stream(file.get("libraries").getAsJsonArray().spliterator(), false)
                .map(info -> new LibraryDownload(info.getAsJsonObject().get("name").getAsString(), info.getAsJsonObject().get("downloads").getAsJsonObject().get("artifact").getAsJsonObject().get("url").getAsString()))
                .collect(Collectors.toList());

        return new VersionFile(clientJar, serverJar, libraries);
    }

    public static class MinecraftDownload {
        private String sha1;
        private String url;

        public MinecraftDownload(String sha1, String url) {
            this.sha1 = sha1;
            this.url = url;
        }

        public String sha1() {
            return sha1;
        }

        public String url() {
            return url;
        }
    }

    public static class LibraryDownload {
        private String name;
        private String url;

        public LibraryDownload(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String name() {
            return name;
        }

        public String url() {
            return url;
        }
    }
}
