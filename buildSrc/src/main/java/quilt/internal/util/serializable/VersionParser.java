package quilt.internal.util.serializable;

import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;
import org.quiltmc.launchermeta.version.v1.Version;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class VersionParser extends NonSerializableObjectParser<Version> {
    public VersionParser(File versionSource) {
        super(versionSource);
    }

    @Override
    protected Version parse(File versionSource) {
        try {
            return Version.fromString(FileUtils.readFileToString(versionSource, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new GradleException("Failed to read version file", e);
        }
    }
}
