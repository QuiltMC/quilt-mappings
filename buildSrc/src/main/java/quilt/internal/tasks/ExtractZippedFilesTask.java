package quilt.internal.tasks;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.OutputDirectory;

public abstract class ExtractZippedFilesTask extends AbstractExtractZipTask {
    @OutputDirectory
    public abstract DirectoryProperty getExtractionDest();

    public ExtractZippedFilesTask(String group) {
        super(group);
    }

    @Override
    protected void extractImpl(FileTree filteredZipTree) {
        filteredZipTree.visit(fileVisitDetails ->
            fileVisitDetails.copyTo(
                this.getExtractionDest().file(fileVisitDetails.getRelativePath().getPathString()).get().getAsFile()
            )
        );
    }
}
