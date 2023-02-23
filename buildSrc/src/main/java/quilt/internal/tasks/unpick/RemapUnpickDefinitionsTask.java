package quilt.internal.tasks.unpick;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import daomephsta.unpick.constantmappers.datadriven.parser.FieldKey;
import daomephsta.unpick.constantmappers.datadriven.parser.MethodKey;
import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Reader;
import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Remapper;
import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Writer;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingNsCompleter;
import javax.inject.Inject;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;
import org.jetbrains.annotations.VisibleForTesting;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;
import quilt.internal.util.UnpickUtil;

import net.fabricmc.mappingio.format.Tiny2Reader;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public abstract class RemapUnpickDefinitionsTask extends DefaultMappingsTask {
    public static final String TASK_NAME = "remapUnpickDefinitions";
    private final RegularFileProperty input;
    private final RegularFileProperty mappings;
    private final RegularFileProperty output;

    public RemapUnpickDefinitionsTask() {
        super(Constants.Groups.UNPICK);
        input = getProject().getObjects().fileProperty();
        CombineUnpickDefinitionsTask combineUnpickDefinitionsTask = getTaskByType(CombineUnpickDefinitionsTask.class);
        input.set(combineUnpickDefinitionsTask.getOutput());
        mappings = getProject().getObjects().fileProperty();
        output = getProject().getObjects().fileProperty();
    }

    @InputFile
    public RegularFileProperty getInput() {
        return input;
    }

    @InputFile
    public RegularFileProperty getMappings() {
        return mappings;
    }

    @OutputFile
    public RegularFileProperty getOutput() {
        return output;
    }

    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    @TaskAction
    public void run() {
        WorkQueue workQueue = getWorkerExecutor().noIsolation();
        workQueue.submit(RemapAction.class, parameters -> {
            parameters.getInput().set(getInput().get().getAsFile());
            parameters.getMappings().set(getMappings().get().getAsFile());
            parameters.getOutput().set(getOutput().get().getAsFile());
        });
    }

    @VisibleForTesting
    public static void remapUnpickDefinitions(Path input, Path mappings, Path output) {
        try {
            Files.deleteIfExists(output);

            Map<String, String> classMappings = new HashMap<>();
            Map<MethodKey, String> methodMappings = new HashMap<>();
            Map<FieldKey, String> fieldMappings = new HashMap<>();
            String fromM = "named";
            String toM = Constants.PER_VERSION_MAPPINGS_NAME;

            try (BufferedReader reader = Files.newBufferedReader(mappings)) {
                MemoryMappingTree mappingTree = new MemoryMappingTree();
                // Use target namespace as fallback to source namespace
                MappingVisitor visitor = new MappingNsCompleter(mappingTree, Collections.singletonMap(fromM, toM));
                Tiny2Reader.read(reader, visitor);

                for (MappingTree.ClassMapping classMapping : mappingTree.getClasses()) {
                    classMappings.put(classMapping.getName(fromM), classMapping.getName(toM));

                    for (MappingTree.MethodMapping methodMapping : classMapping.getMethods()) {
                        methodMappings.put(
                            new MethodKey(classMapping.getName(fromM), methodMapping.getName(fromM), methodMapping.getDesc(fromM)),
                            methodMapping.getName(toM)
                        );
                    }

                    for (MappingTree.FieldMapping fieldMapping : classMapping.getFields()) {
                        fieldMappings.put(
                            new FieldKey(classMapping.getName(fromM), fieldMapping.getName(fromM)),
                            fieldMapping.getName(toM)
                        );
                    }
                }
            }

            try (UnpickV2Reader reader = new UnpickV2Reader(Files.newInputStream(input))) {
                UnpickV2Writer writer = new UnpickV2Writer();
                reader.accept(new UnpickV2Remapper(classMappings, methodMappings, fieldMappings, writer));
                Files.writeString(output, UnpickUtil.getLfOutput(writer));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public interface RemapParameters extends WorkParameters {
        @Input
        Property<File> getInput();

        @Input
        Property<File> getMappings();

        @OutputFile
        Property<File> getOutput();
    }

    public abstract static class RemapAction implements WorkAction<RemapParameters> {
        @Inject
        public RemapAction() {
        }

        @Override
        public void execute() {
            Path input = getParameters().getInput().get().toPath();
            Path mappings = getParameters().getMappings().get().toPath();
            Path output = getParameters().getOutput().get().toPath();
            remapUnpickDefinitions(input, mappings, output);
        }
    }
}
