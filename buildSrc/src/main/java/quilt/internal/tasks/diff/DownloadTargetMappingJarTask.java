package quilt.internal.tasks.diff;

import java.io.File;
import java.io.IOException;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

public class DownloadTargetMappingJarTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "downloadTargetMappingsJar";

    @OutputFile
    private final RegularFileProperty targetMappingsFile;

    @OutputFile
    private final RegularFileProperty targetUnpickFile;

    @OutputFile
    private final RegularFileProperty targetUnpickDefinitionsFile;

    @Internal
    private final RegularFileProperty targetUnpickRemappedDefinitionsFile;

    @OutputFile
    private final RegularFileProperty targetUnpickConstantsFile;

    public static final String TARGET_MAPPINGS = ".gradle/targets";

    public DownloadTargetMappingJarTask() {
        super("diff");

        CheckTargetVersionExistsTask checkExists = this.getTaskByType(CheckTargetVersionExistsTask.class);
        this.onlyIf(task -> checkExists.getTargetVersion().isPresent());

        this.dependsOn(CheckTargetVersionExistsTask.TASK_NAME);

        targetMappingsFile = getProject().getObjects().fileProperty();
        targetUnpickFile = getProject().getObjects().fileProperty();
        targetUnpickDefinitionsFile = getProject().getObjects().fileProperty();
        targetUnpickRemappedDefinitionsFile = getProject().getObjects().fileProperty();
        targetUnpickConstantsFile = getProject().getObjects().fileProperty();
        targetMappingsFile.convention(() -> getProject().file(TARGET_MAPPINGS + "/quilt-mappings-" + checkExists.getTargetVersion().orElse(Constants.MAPPINGS_VERSION) + "/mappings/mappings.tiny"));
        targetUnpickFile.convention(() -> getProject().file(TARGET_MAPPINGS + "/quilt-mappings-" + checkExists.getTargetVersion().orElse(Constants.MAPPINGS_VERSION) + "/extras/unpick.json"));
        targetUnpickDefinitionsFile.convention(() -> getProject().file(TARGET_MAPPINGS + "/quilt-mappings-" + checkExists.getTargetVersion().orElse(Constants.MAPPINGS_VERSION) + "/extras/definitions.unpick"));
        targetUnpickRemappedDefinitionsFile.convention(() -> getProject().file(TARGET_MAPPINGS + "/quilt-mappings-" + checkExists.getTargetVersion().orElse(Constants.MAPPINGS_VERSION) + "remapped-unpick.unpick"));
        targetUnpickConstantsFile.convention(() -> getProject().file(TARGET_MAPPINGS + "/quilt-mappings-" + checkExists.getTargetVersion().orElse(Constants.MAPPINGS_VERSION) + "-constants.jar"));
    }

    @TaskAction
    public void downloadTargetMappings() throws IOException {
        String targetVersion = this.getTaskByType(CheckTargetVersionExistsTask.class).getTargetVersion().get();

        File targetMappingsJar = getProject().file(TARGET_MAPPINGS + "/quilt-mappings-" + targetVersion + "-v2.jar");
        this.startDownload()
                .src("https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-mappings/" + targetVersion + "/quilt-mappings-" + targetVersion + "-v2.jar")
                .dest(targetMappingsJar)
                .download();

        this.startDownload()
                .src("https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-mappings/" + targetVersion + "/quilt-mappings-" + targetVersion + "-constants.jar")
                .dest(targetUnpickConstantsFile.getAsFile().get())
                .download();

        getProject()
                .zipTree(targetMappingsJar)
                .getAsFileTree()
                .visit(fileVisitDetails -> fileVisitDetails.copyTo(getProject().file(TARGET_MAPPINGS + "/quilt-mappings-" + targetVersion + "/" + fileVisitDetails.getRelativePath())));
    }

    public RegularFileProperty getTargetMappingsFile() {
        return targetMappingsFile;
    }

    public RegularFileProperty getTargetUnpickFile() {
        return targetUnpickFile;
    }

    public RegularFileProperty getTargetUnpickConstantsFile() {
        return targetUnpickConstantsFile;
    }

    public RegularFileProperty getTargetUnpickDefinitionsFile() {
        return targetUnpickDefinitionsFile;
    }

    public RegularFileProperty getTargetUnpickRemappedDefinitionsFile() {
        return targetUnpickRemappedDefinitionsFile;
    }
}
