package quilt.internal.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public abstract class DummyPlugin implements Plugin<Project> {
    @Override
    public void apply(@NotNull Project project) { }
}
