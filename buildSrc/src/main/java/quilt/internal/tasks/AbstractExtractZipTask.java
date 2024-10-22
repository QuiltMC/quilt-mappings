package quilt.internal.tasks;

import org.gradle.api.Action;
import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.util.PatternFilterable;

import javax.inject.Inject;
import java.io.IOException;

public abstract class AbstractExtractZipTask extends DefaultMappingsTask {
    @Optional
    @Input
    protected abstract Property<Action<? super PatternFilterable>> getFilter();

    @InputFile
    public abstract RegularFileProperty getZippedFile();

    @Inject
    protected abstract ArchiveOperations getArchiveOperations();

    public AbstractExtractZipTask(String group) {
        super(group);
    }

    @TaskAction
    public final void extract() throws IOException {
        this.extractImpl(
            this.getArchiveOperations().zipTree(this.getZippedFile())
                .matching(this.getFilter().getOrElse(unused -> { }))
        );
    }

    protected abstract void extractImpl(FileTree filteredZipTree) throws IOException;
}
