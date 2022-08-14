package quilt.internal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;
import quilt.internal.tasks.build.*;
import quilt.internal.tasks.jarmapping.MapNamedJarTask;
import quilt.internal.tasks.jarmapping.MapPerVersionMappingsJarTask;
import quilt.internal.tasks.lint.MappingLintTask;
import quilt.internal.tasks.setup.*;
import quilt.internal.tasks.unpick.CombineUnpickDefinitionsTask;
import quilt.internal.tasks.unpick.RemapUnpickDefinitionsTask;
import quilt.internal.tasks.unpick.gen.OpenGlConstantUnpickGenerator;

public class MappingsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        target.getExtensions().add("mappings", new MappingsExtension(target));

        TaskContainer tasks = target.getTasks();
        tasks.create(DownloadVersionsManifestTask.TASK_NAME, DownloadVersionsManifestTask.class);
        tasks.create(DownloadWantedVersionManifestTask.TASK_NAME, DownloadWantedVersionManifestTask.class);
        tasks.create(DownloadMinecraftJarsTask.TASK_NAME, DownloadMinecraftJarsTask.class);
        tasks.create(ExtractServerJarTask.TASK_NAME, ExtractServerJarTask.class);
        tasks.create(MergeJarsTask.TASK_NAME, MergeJarsTask.class);
        tasks.create(DownloadMinecraftLibrariesTask.TASK_NAME, DownloadMinecraftLibrariesTask.class);

        tasks.create(DownloadPerVersionMappingsTask.TASK_NAME, DownloadPerVersionMappingsTask.class);
        tasks.create(InvertPerVersionMappingsTask.TASK_NAME, InvertPerVersionMappingsTask.class);
        tasks.create(BuildMappingsTinyTask.TASK_NAME, BuildMappingsTinyTask.class);
        tasks.create(MergeTinyTask.TASK_NAME, MergeTinyTask.class);
        tasks.create(MergeTinyV2Task.TASK_NAME, MergeTinyV2Task.class);
        tasks.create(TinyJarTask.TASK_NAME, TinyJarTask.class);
        tasks.create(CompressTinyTask.TASK_NAME, CompressTinyTask.class);
        tasks.create(DropInvalidMappingsTask.TASK_NAME, DropInvalidMappingsTask.class);

        tasks.create(MapPerVersionMappingsJarTask.TASK_NAME, MapPerVersionMappingsJarTask.class);
        tasks.create(MapNamedJarTask.TASK_NAME, MapNamedJarTask.class);

        tasks.create(CombineUnpickDefinitionsTask.TASK_NAME, CombineUnpickDefinitionsTask.class);
        tasks.create(RemapUnpickDefinitionsTask.TASK_NAME, RemapUnpickDefinitionsTask.class);
        tasks.create(OpenGlConstantUnpickGenerator.TASK_NAME, OpenGlConstantUnpickGenerator.class);

        tasks.create(GeneratePackageInfoMappingsTask.TASK_NAME, GeneratePackageInfoMappingsTask.class);
        tasks.create(MappingLintTask.TASK_NAME, MappingLintTask.class);

        tasks.create(DownloadIntermediaryMappingsTask.TASK_NAME, DownloadIntermediaryMappingsTask.class);
        tasks.create(InvertIntermediaryMappingsTask.TASK_NAME, InvertIntermediaryMappingsTask.class);
        tasks.create(MergeIntermediaryTask.TASK_NAME, MergeIntermediaryTask.class);
    }

    public static MappingsExtension getExtension(Project project) {
        return project.getExtensions().getByType(MappingsExtension.class);
    }
}
