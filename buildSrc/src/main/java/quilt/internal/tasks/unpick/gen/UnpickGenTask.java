package quilt.internal.tasks.unpick.gen;

import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.OutputFiles;

public interface UnpickGenTask extends Task {
    // TODO would it be better to just assume UnpickGenTasks *only* output unpick definitions?
    //  Then in MappingsPlugin we could just do:
    //  combineUnpickDefinitions.getUnpickDefinitions().builtBy(project.getTasks().withType(UnpickGenTask.class));
    @OutputFiles
    FileCollection getGeneratedUnpickDefinitions();
}
