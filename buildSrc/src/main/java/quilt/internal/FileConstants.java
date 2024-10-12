package quilt.internal;

import java.io.File;

import org.gradle.api.Project;
import quilt.internal.tasks.lint.DownloadDictionaryFileTask;

// TODO eliminate these, either by putting them directly in QuiltMappingsPlugin#apply
//  or by making them configurable properties of QuiltMappingsExtension
public class FileConstants {
    public final File tempDir;

    public FileConstants(Project project) {
        this.tempDir = project.file(".gradle/temp");
    }
}
