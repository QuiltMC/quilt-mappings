package quilt.internal;

import java.io.File;

import org.gradle.api.Project;
import quilt.internal.tasks.lint.DownloadDictionaryFileTask;

public class FileConstants {
    public final File buildDir;
    public final File cacheFilesMinecraft;
    public final File tempDir;
    public final File perVersionMappingsJar;
    public final File unpickedJar;
    public final File namedJar;
    public final File libraries;

    public final File dictionaryDir;
    public final File dictionaryFile;

    public FileConstants(Project project) {
        this.buildDir = project.file("build/mappings");
        this.cacheFilesMinecraft = project.file(".gradle/minecraft");
        this.tempDir = project.file(".gradle/temp");
        this.libraries = new File(this.cacheFilesMinecraft, "libraries");

        this.perVersionMappingsJar =
            project.file(Constants.MINECRAFT_VERSION + "-" + Constants.PER_VERSION_MAPPINGS_NAME + ".jar");
        this.unpickedJar = project.file(
            Constants.MINECRAFT_VERSION + "-" + Constants.PER_VERSION_MAPPINGS_NAME + "-unpicked.jar"
        );
        this.namedJar = project.file(Constants.MINECRAFT_VERSION + "-named.jar");

        this.dictionaryDir = project.file(".gradle/lint/dictionary");
        this.dictionaryFile =
            project.file(this.dictionaryDir.toPath().resolve(DownloadDictionaryFileTask.REVISION + ".txt"));
    }
}
