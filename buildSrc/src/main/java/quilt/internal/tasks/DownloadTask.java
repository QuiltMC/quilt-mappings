package quilt.internal.tasks;

import quilt.internal.util.DownloadImmediate;

public interface DownloadTask extends MappingsTask {
    default DownloadImmediate.Builder startDownload() {
        return new DownloadImmediate.Builder(this);
    }
}
