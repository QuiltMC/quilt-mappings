package quilt.internal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import quilt.internal.tasks.setup.DownloadMinecraftJarsTask;
import quilt.internal.tasks.setup.DownloadMinecraftLibrariesTask;
import quilt.internal.tasks.setup.DownloadVersionsManifestTask;
import quilt.internal.tasks.setup.DownloadWantedVersionManifestTask;
import quilt.internal.tasks.setup.MergeJarsTask;

public class MappingsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        target.getExtensions().add("Mappings Extension", new MappingsExtension(target));

        target.getTasks().create("downloadVersionsManifest", DownloadVersionsManifestTask.class);
        target.getTasks().create("downloadWantedVersionManifest", DownloadWantedVersionManifestTask.class);
        target.getTasks().create("downloadMinecraftJars", DownloadMinecraftJarsTask.class);
        target.getTasks().create("mergeJars", MergeJarsTask.class);
        target.getTasks().create("downloadMinecraftLibraries", DownloadMinecraftLibrariesTask.class);
    }

    public static MappingsExtension getExtension(Project project) {
        return project.getExtensions().getByType(MappingsExtension.class);
    }
}
