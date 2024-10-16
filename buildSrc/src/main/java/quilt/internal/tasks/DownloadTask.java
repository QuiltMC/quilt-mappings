package quilt.internal.tasks;

import quilt.internal.util.Downloader;

public interface DownloadTask extends MappingsTask {
    default Downloader startDownload() {
        return new Downloader(this);
    }
}
