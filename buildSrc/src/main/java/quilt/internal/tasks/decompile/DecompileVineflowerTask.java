package quilt.internal.tasks.decompile;

import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskContainer;
import quilt.internal.decompile.Decompilers;
import quilt.internal.plugin.ProcessMappingsPlugin;

import java.io.IOException;

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
        FileUtils.deleteDirectory(this.getOutput().get().getAsFile());

        super.decompile();

        // TODO see if updating vineflower fixes non-fatal exception:
        //  java.nio.file.FileSystemAlreadyExistsException
        //  in org.jetbrains.java.decompiler.main.plugins.JarPluginLoader:init
        //  if not, manually close the FileSystem here
    }
}
