package quilt.internal;

import java.io.File;

import org.gradle.api.Project;

public class FileConstants {
    public final File mappingsDir;
    public final File cacheFilesMinecraft;
    public final File tempDir;
    public final File hashedMojmapJar;
    public final File unpickedJar;
    public final File namedJar;
    public final File libraries;
    public final File libs;

    public FileConstants(Project project) {
        mappingsDir = project.file("mappings");
        cacheFilesMinecraft = project.file(".gradle/minecraft");
        tempDir = project.file(".gradle/temp");
        hashedMojmapJar = project.file(Constants.MINECRAFT_VERSION + "-" + Constants.PER_VERSION_MAPPINGS_NAME + ".jar");
        unpickedJar = project.file(Constants.MINECRAFT_VERSION + "-" + Constants.PER_VERSION_MAPPINGS_NAME + "-unpicked.jar");
        namedJar = project.file(Constants.MINECRAFT_VERSION + "-named.jar");

        libraries = new File(cacheFilesMinecraft, "libraries");
        libs = new File("build/libs/");
    }
}
