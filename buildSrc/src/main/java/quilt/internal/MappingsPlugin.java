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
        tasks.create("downloadVersionsManifest", DownloadVersionsManifestTask.class);
        tasks.create("downloadWantedVersionManifest", DownloadWantedVersionManifestTask.class);
        tasks.create("downloadMinecraftJars", DownloadMinecraftJarsTask.class);
        tasks.create("mergeJars", MergeJarsTask.class);
        tasks.create("downloadMinecraftLibraries", DownloadMinecraftLibrariesTask.class);

        tasks.create("checkMappings", CheckMappingsTask.class);
        tasks.create("downloadHashedMojmap", DownloadPerVersionMappingsTask.class);
        tasks.create("invertHashedMojmap", InvertPerVersionMappingsTask.class);
        tasks.create("buildMappingsTiny", BuildMappingsTinyTask.class);
        tasks.create("mergeTiny", MergeTinyTask.class);
        tasks.create("tinyJar", TinyJarTask.class);
        tasks.create("compressTiny", CompressTinyTask.class);
    }

    public static MappingsExtension getExtension(Project project) {
        return project.getExtensions().getByType(MappingsExtension.class);
    }
}
