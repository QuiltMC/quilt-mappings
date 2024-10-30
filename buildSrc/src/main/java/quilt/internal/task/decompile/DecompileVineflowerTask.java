package quilt.internal.task.decompile;

import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskContainer;
import quilt.internal.decompile.Decompilers;
import quilt.internal.plugin.ProcessMappingsPlugin;

import java.io.File;
import java.io.IOException;

import static quilt.internal.util.ProviderUtil.toOptional;

public abstract class DecompileVineflowerTask extends DecompileTask {
    /**
     * {@linkplain TaskContainer#register Registered} by {@link ProcessMappingsPlugin}.
     */
    public static final String DECOMPILE_VINEFLOWER_TASK_NAME = "decompileVineflower";

    public DecompileVineflowerTask() {
        this.getDecompiler().set(Decompilers.VINEFLOWER);
        this.getDecompiler().finalizeValue();
    }

    @Override
    @TaskAction
    public void decompile() throws IOException {
        final File outputDir = this.getOutput().get().getAsFile();
        if (outputDir.exists()) {
            try {
                FileUtils.cleanDirectory(outputDir);
            } catch (IOException e) {
                throw new GradleException("Failed to clean old output", e);
            }
        }

        // TODO QUESTION this sometimes logs a non-fatal exception:
        //  java.nio.file.FileSystemAlreadyExistsException
        //  it's thrown from
        //  org.jetbrains.java.decompiler.main.plugins.JarPluginLoader:init
        //  it seems harmless
        super.decompile();
    }
}
