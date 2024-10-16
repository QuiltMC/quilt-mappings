package quilt.internal.tasks;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.util.PatternFilterable;

import java.io.File;
import java.io.IOException;

public abstract class ExtractSingleZippedFileTask extends AbstractExtractZipTask {
    @OutputFile
    public abstract RegularFileProperty getExtractionDest();

    /**
     * @param group the task group
     * @param filter a filter that selects exactly one file to be extracted from the
     * {@link #getZippedFile() zippedFile}
     */
    public ExtractSingleZippedFileTask(String group, Action<? super PatternFilterable> filter) {
        super(group);

        this.getFilter().set(filter);
        this.getFilter().finalizeValue();
    }

    protected void extractImpl(FileTree filteredZipTree) throws IOException {
        final File singleFile;
        try {
            singleFile = filteredZipTree.getSingleFile();
        } catch (IllegalStateException e) {
            throw new GradleException("Filter did not select exactly one file", e);
        }

        FileUtils.copyFile(
            singleFile,
            this.getExtractionDest().get().getAsFile()
        );
    }
}
