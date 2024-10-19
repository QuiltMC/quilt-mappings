package quilt.internal.util;

import org.quiltmc.launchermeta.version.v1.DownloadableFile;

import java.io.Serializable;

public class SerializableDownloadableFile extends DownloadableFile implements Serializable {
    public SerializableDownloadableFile(DownloadableFile file) {
        super(file.getSha1(), file.getSize(), file.getUrl());
    }
}
