package quilt.internal.util.json;

import java.util.List;

import com.google.gson.Gson;
import quilt.internal.tasks.setup.DownloadVersionsManifestTask;

public final class ManifestFile {
    private List<Version> versions;

    public ManifestFile(List<Version> versions) {
        this.versions = versions;
    }

    public static ManifestFile fromJson(String json) {
        return new Gson().fromJson(json, ManifestFile.class);
    }

    public List<Version> versions() {
        return versions;
    }

    public static final class Version {
        private String id;
        private String url;
        private String releaseTime;

        public Version(String id, String url, String releaseTime) {
            this.id = id;
            this.url = url;
            this.releaseTime = releaseTime;
        }

        public String id() {
            return id;
        }

        public String url() {
            return url;
        }

        public String releaseTime() {
            return releaseTime;
        }
    }
}
