package quilt.internal.task;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.OutputDirectory;

public abstract class ExtractZippedFilesTask extends AbstractExtractZipTask {
    @OutputDirectory
    public abstract DirectoryProperty getExtractionDest();

    @Override
    protected void extractImpl(FileTree filteredZipTree) {
        filteredZipTree.visit(fileVisitDetails ->
            fileVisitDetails.copyTo(
                this.getExtractionDest().file(fileVisitDetails.getRelativePath().getPathString()).get().getAsFile()
            )
        );
    }
}
