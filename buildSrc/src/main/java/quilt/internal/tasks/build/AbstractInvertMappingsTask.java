package quilt.internal.tasks.build;

import net.fabricmc.stitch.commands.tinyv2.CommandReorderTinyV2;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import java.io.File;

public abstract class AbstractInvertMappingsTask extends DefaultMappingsTask {
    @InputFile
    protected final RegularFileProperty input;

    @OutputFile
    public File invertedTinyFile;

    private final String mappingsName;

    public AbstractInvertMappingsTask(String mappingsName) {
        super(Constants.Groups.BUILD_MAPPINGS_GROUP);

        input = getProject().getObjects().fileProperty();
        invertedTinyFile = new File(fileConstants.cacheFilesMinecraft, String.format("%s-%s-inverted.tiny", Constants.MINECRAFT_VERSION, mappingsName));

        this.mappingsName = mappingsName;
    }

    @TaskAction
    public void invertMappings() throws Exception {
        getLogger().lifecycle(":building inverted {}", mappingsName);


        String[] args = {
                input.get().getAsFile().getAbsolutePath(),
                invertedTinyFile.getAbsolutePath(),
                mappingsName, "official"
        };

        new CommandReorderTinyV2().run(args);
    }

    public File getInvertedTinyFile() {
        return invertedTinyFile;
    }

    public RegularFileProperty getInput() {
        return input;
    }
}
