package quilt.internal.tasks;

import org.gradle.api.Action;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.util.PatternFilterable;

import java.io.IOException;

public abstract class AbstractExtractZipTask extends DefaultMappingsTask {
    @Optional
    @Input
    protected abstract Property<Action<? super PatternFilterable>> getFilter();

    @InputFile
    public abstract RegularFileProperty getZippedFile();

    private final FileTree zipTree;

    public AbstractExtractZipTask(String group) {
        super(group);

        // zipTree accesses the passed path lazily so passing jarFile here is ok
        this.zipTree = this.getProject().zipTree(this.getZippedFile());
    }

    @TaskAction
    public final void extract() throws IOException {
        this.extractImpl(this.zipTree.matching(this.getFilter().getOrElse(unused -> { })));
    }

    protected abstract void extractImpl(FileTree filteredZipTree) throws IOException;
}
