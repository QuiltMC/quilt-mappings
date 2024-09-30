package quilt.internal.tasks.setup;

import org.apache.commons.io.FileUtils;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import java.io.IOException;

public abstract class ExtractTinyMappingsTask extends DefaultMappingsTask {
    public static final String TINY_MAPPINGS_FILE_NAME_SUFFIX = "mappings.tiny";

    @InputFile
    public abstract RegularFileProperty getJarFile();

    @OutputFile
    public abstract RegularFileProperty getTinyFile();

    private final FileTree jarZipTree;

    public ExtractTinyMappingsTask() {
        super(Constants.Groups.SETUP_GROUP);

        // zipTree accesses the passed path lazily so passing jarFile here is ok
        this.jarZipTree = this.getProject().zipTree(this.getJarFile());
    }

    @TaskAction
    public void extractTinyMappings() throws IOException {
        FileUtils.copyFile(
            this.jarZipTree
                .getFiles()
                .stream()
                .filter(file -> file.getName().endsWith(TINY_MAPPINGS_FILE_NAME_SUFFIX))
                .findFirst()
                .orElseThrow(),
            this.getTinyFile().get().getAsFile()
        );
    }
}
