package quilt.internal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;
import quilt.internal.tasks.build.BuildMappingsTinyTask;
import quilt.internal.tasks.build.CheckMappingsTask;
import quilt.internal.tasks.build.CompressTinyTask;
import quilt.internal.tasks.build.DownloadPerVersionMappingsTask;
import quilt.internal.tasks.build.InvertPerVersionMappingsTask;
import quilt.internal.tasks.build.MergeTinyTask;
import quilt.internal.tasks.build.TinyJarTask;
import quilt.internal.tasks.jarmapping.MapPerVersionMappingsJarTask;
import quilt.internal.tasks.setup.DownloadMinecraftJarsTask;
import quilt.internal.tasks.setup.DownloadMinecraftLibrariesTask;
import quilt.internal.tasks.setup.DownloadVersionsManifestTask;
import quilt.internal.tasks.setup.DownloadWantedVersionManifestTask;
import quilt.internal.tasks.setup.MergeJarsTask;

public class MappingsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project target) {
        target.getExtensions().add("Mappings Extension", new MappingsExtension(target));

        TaskContainer tasks = target.getTasks();
        tasks.create(DownloadVersionsManifestTask.TASK_NAME, DownloadVersionsManifestTask.class);
        tasks.create(DownloadWantedVersionManifestTask.TASK_NAME, DownloadWantedVersionManifestTask.class);
        tasks.create(DownloadMinecraftJarsTask.TASK_NAME, DownloadMinecraftJarsTask.class);
        tasks.create(MergeJarsTask.TASK_NAME, MergeJarsTask.class);
        tasks.create(DownloadMinecraftLibrariesTask.TASK_NAME, DownloadMinecraftLibrariesTask.class);

        tasks.create(CheckMappingsTask.TASK_NAME, CheckMappingsTask.class);
        tasks.create(DownloadPerVersionMappingsTask.TASK_NAME, DownloadPerVersionMappingsTask.class);
        tasks.create(InvertPerVersionMappingsTask.TASK_NAME, InvertPerVersionMappingsTask.class);
        tasks.create(BuildMappingsTinyTask.TASK_NAME, BuildMappingsTinyTask.class);
        tasks.create(MergeTinyTask.TASK_NAME, MergeTinyTask.class);
        tasks.create(TinyJarTask.TASK_NAME, TinyJarTask.class);
        tasks.create(CompressTinyTask.TASK_NAME, CompressTinyTask.class);

        tasks.create(MapPerVersionMappingsJarTask.TASK_NAME, MapPerVersionMappingsJarTask.class);
    }

    public static MappingsExtension getExtension(Project project) {
        return project.getExtensions().getByType(MappingsExtension.class);
    }
}
