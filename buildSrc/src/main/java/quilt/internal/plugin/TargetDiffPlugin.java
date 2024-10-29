package quilt.internal.plugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;
import quilt.internal.Constants;
import quilt.internal.QuiltMappingsExtension;
import quilt.internal.decompile.javadoc.MappingsJavadocProvider;
import quilt.internal.plugin.abstraction.MappingsProjectPlugin;
import quilt.internal.task.build.MappingsV2JarTask;
import quilt.internal.task.decompile.DecompileVineflowerTask;
import quilt.internal.task.diff.DecompileTargetVineflowerTask;
import quilt.internal.task.diff.DiffDirectoriesTask;
import quilt.internal.task.diff.DownloadTargetMappingJarTask;
import quilt.internal.task.diff.DownloadTargetMetaFileTask;
import quilt.internal.task.diff.ExtractTargetMappingJarTask;
import quilt.internal.task.diff.RemapTargetMinecraftJarTask;
import quilt.internal.task.diff.RemapTargetUnpickDefinitionsTask;
import quilt.internal.task.diff.TargetVersionConsumingTask;
import quilt.internal.task.diff.UnpickTargetJarTask;
import quilt.internal.task.diff.UnpickVersionsMatchConsumingTask;
import quilt.internal.task.setup.DownloadMinecraftLibrariesTask;

import java.io.FileReader;
import java.io.IOException;

import static quilt.internal.Constants.UNPICK_NAME;
import static quilt.internal.task.build.MappingsV2JarTask.JAR_MAPPINGS_PATH;
import static quilt.internal.task.build.MappingsV2JarTask.V2_CLASSIFIER;
import static quilt.internal.task.jarmapping.MapJarTask.UNPICKED_CLASSIFIER;
import static quilt.internal.task.jarmapping.MapNamedJarTask.NAMED_CLASSIFIER;
import static quilt.internal.task.setup.ConstantsJarTask.CONSTANTS_CLASSIFIER;
import static quilt.internal.task.unpick.gen.UnpickGenTask.UNPICK_EXTENSION;

/**
 * {@linkplain TaskContainer#register Registers} tasks that download the latest published Quilt Mappings for the current
 * {@link QuiltMappingsExtension#getMinecraftVersion() minecraftVersion} so the
 * {@value DiffDirectoriesTask#GENERATE_DIFF_TASK_NAME} task can {@value DiffDirectoriesTask#DIFF_COMMAND}
 * them with this project's mappings.
 * <p>
 * The generated {@value DiffDirectoriesTask#DIFF_COMMAND} is useful when reviewing new mappings.
 * <p>
 * Additionally:
 * <ul>
 *     <li> {@linkplain TaskCollection#configureEach(Action) configures}
 *          {@link TargetVersionConsumingTask}s with the following defaults:
 *          <ul>
 *              <li> {@link TargetVersionConsumingTask#getTargetVersion() targetVersion}:
 *                   {@value DownloadTargetMetaFileTask#DOWNLOAD_TARGET_META_FILE_TASK_NAME}'s
 *                   {@linkplain DownloadTargetMetaFileTask#provideTargetVersion() provided target version}
 *              <li> run {@link Task#onlyIf(Spec) onlyIf} their
 *                   {@link TargetVersionConsumingTask#getTargetVersion() targetVersion}
 *                   {@link Provider#isPresent() isPresent}
 *          </ul>
 *     <li> {@linkplain TaskCollection#configureEach(Action) configures}
 *          {@link UnpickVersionsMatchConsumingTask}s with the following defaults:
 *          <ul>
 *              <li> {@link UnpickVersionsMatchConsumingTask#getUnpickVersionsMatch() unpickVersionsMatch}:
 *                   this plugin's {@linkplain #provideUnpickVersionsMatch provided check}
 *                   comparing {@link QuiltMappingsExtension}'s
 *                   {@link QuiltMappingsExtension#getUnpickVersion() unpickVersion} and the
 *                   {@linkplain MappingsV2JarTask#JAR_UNPICK_META_PATH unpick meta} from the
 *                   {@value ExtractTargetMappingJarTask#EXTRACT_TARGET_MAPPINGS_JAR_TASK_NAME} task's
 *                   {@linkplain ExtractTargetMappingJarTask#getExtractionDest extracted jar}
 *              <li> run {@link Task#onlyIf(Spec) onlyIf}
 *                   {@link UnpickVersionsMatchConsumingTask#getUnpickVersionsMatch() unpickVersionsMatch}
 *                   is {@code true}
 *          </ul>
 * </ul>
 */
public abstract class TargetDiffPlugin implements MappingsProjectPlugin {
    @Override
    public void apply(@NotNull Project project) {
        final PluginContainer plugins = project.getPlugins();

        final QuiltMappingsExtension ext = plugins.apply(QuiltMappingsBasePlugin.class).getExt();

        final MinecraftJarsPlugin.Tasks minecraftJarsTasks =
            plugins.apply(MinecraftJarsPlugin.class).getTasks();
        final TaskProvider<DownloadMinecraftLibrariesTask> downloadMinecraftLibraries =
            minecraftJarsTasks.downloadMinecraftLibraries();

        final ProcessMappingsPlugin.Tasks processMappingsTasks =
            plugins.apply(ProcessMappingsPlugin.class).getTasks();
        final var decompileVineflower =
            processMappingsTasks.decompileVineflower();

        // register this plugin's tasks
        final TaskContainer tasks = project.getTasks();

        {
            final var downloadTargetMetaFile = tasks.register(
                DownloadTargetMetaFileTask.DOWNLOAD_TARGET_META_FILE_TASK_NAME,
                DownloadTargetMetaFileTask.class,
                task -> {
                    task.getMinecraftVersion().convention(ext.getMinecraftVersion());

                    task.getDest().convention(this.provideMinecraftBuildFile(
                        task.getMinecraftVersion().map(createQuiltFileNameBuilder(".json"))
                    ));
                }
            );

            // put mapped provider in a property so all tasks use the same cached value
            final Property<String> targetVersion = this.getObjects().property(String.class);
            targetVersion.set(
                downloadTargetMetaFile.flatMap(DownloadTargetMetaFileTask::provideTargetVersion)
            );

            tasks.withType(TargetVersionConsumingTask.class).configureEach(task -> {
                task.getTargetVersion().convention(targetVersion);

                task.onlyIf(unused -> task.getTargetVersion().isPresent());
            });
        }

        final var downloadTargetMappingsJar = tasks.register(
            DownloadTargetMappingJarTask.DOWNLOAD_TARGET_MAPPINGS_JAR_TASK_NAME,
            DownloadTargetMappingJarTask.class,
            task -> {
                task.getTargetUnpickConstantsFile().convention(
                    this.provideQuiltTargetBuildJarFile(task.getTargetVersion(), CONSTANTS_CLASSIFIER)
                );

                task.getTargetJar().convention(
                    this.provideQuiltTargetBuildJarFile(task.getTargetVersion(), V2_CLASSIFIER)
                );
            }
        );

        final var extractTargetMappingsJar = tasks.register(
            ExtractTargetMappingJarTask.EXTRACT_TARGET_MAPPINGS_JAR_TASK_NAME,
            ExtractTargetMappingJarTask.class,
            task -> {
                task.getZippedFile().convention(
                    downloadTargetMappingsJar.flatMap(DownloadTargetMappingJarTask::getTargetJar)
                );

                task.getExtractionDest().convention(
                    this.provideQuiltTargetBuildDir(task.getTargetVersion(), "")
                );
            }
        );

        {
            // put mapped provider in a property so all tasks use the same cached value
            final Property<Boolean> unpickVersionsMatch = this.getObjects().property(Boolean.class);
            unpickVersionsMatch.set(provideUnpickVersionsMatch(
                ext.getUnpickVersion(),
                extractTargetMappingsJar
                    .flatMap(ExtractTargetMappingJarTask::getExtractionDest)
                    .map(dest -> dest.file(MappingsV2JarTask.JAR_UNPICK_META_PATH))
            ));

            tasks.withType(UnpickVersionsMatchConsumingTask.class).configureEach(task -> {
                task.getUnpickVersionsMatch().convention(unpickVersionsMatch);

                task.onlyIf(unused -> task.getUnpickVersionsMatch().get());
            });
        }

        final var remapTargetUnpickDefinitions = tasks.register(
            RemapTargetUnpickDefinitionsTask.REMAP_TARGET_UNPICK_DEFINITIONS_TASK_NAME,
            RemapTargetUnpickDefinitionsTask.class,
            task -> {
                task.getInput().convention(
                    extractTargetMappingsJar.flatMap(ExtractTargetMappingJarTask::getExtractionDest)
                        .map(dest -> dest.file(MappingsV2JarTask.JAR_UNPICK_DEFINITION_PATH))
                );

                task.getMappings().convention(
                    extractTargetMappingsJar.flatMap(ExtractTargetMappingJarTask::getExtractionDest)
                        .map(dest -> dest.file(JAR_MAPPINGS_PATH))
                );

                task.getOutput().convention(this.provideQuiltTargetBuildFile(
                    task.getTargetVersion(), "remapped-" + UNPICK_NAME, UNPICK_EXTENSION
                ));
            }
        );

        final var unpickTargetJar = tasks.register(
            UnpickTargetJarTask.UNPICK_TARGET_JAR_TASK_NAME,
            UnpickTargetJarTask.class,
            task -> {
                task.getUnpickDefinition().convention(
                    remapTargetUnpickDefinitions.flatMap(RemapTargetUnpickDefinitionsTask::getOutput)
                );

                task.getUnpickConstantsJar().convention(
                    downloadTargetMappingsJar.flatMap(DownloadTargetMappingJarTask::getTargetUnpickConstantsFile)
                );

                task.getOutputFile().convention(
                    this.provideQuiltTargetBuildJarFile(task.getTargetVersion(), UNPICKED_CLASSIFIER)
                );
            }
        );

        final var remapTargetMinecraftJar = tasks.register(
            RemapTargetMinecraftJarTask.REMAP_TARGET_MINECRAFT_JAR_TASK_NAME,
            RemapTargetMinecraftJarTask.class,
            task -> {
                task.getInputJar().convention(unpickTargetJar.flatMap(UnpickTargetJarTask::getOutputFile));

                task.getMappingsFile().convention(
                    extractTargetMappingsJar.flatMap(ExtractTargetMappingJarTask::getExtractionDest)
                        .map(dest -> dest.file(JAR_MAPPINGS_PATH))
                );

                task.getOutputJar().convention(
                    this.provideQuiltTargetBuildJarFile(task.getTargetVersion(), NAMED_CLASSIFIER)
                );
            }
        );

        final var decompileTargetVineflower = tasks.register(
            DecompileTargetVineflowerTask.DECOMPILE_TARGET_VINEFLOWER_TASK_NAME,
            DecompileTargetVineflowerTask.class,
            task -> {
                task.getSources().from(remapTargetMinecraftJar.flatMap(RemapTargetMinecraftJarTask::getOutputJar));

                task.getLibraries().from(
                    downloadMinecraftLibraries.flatMap(DownloadMinecraftLibrariesTask::getLibrariesDir)
                );

                task.getDefaultJavadocSource().convention(MappingsJavadocProvider.provideNamed(
                    extractTargetMappingsJar.flatMap(ExtractTargetMappingJarTask::getExtractionDest)
                        .map(dest -> dest.file(JAR_MAPPINGS_PATH))
                ));

                // TODO move this to build/ once generate-diff.yml uses generateDiff
                task.getOutput().convention(this.getProjectDir().dir("namedTargetSrc"));
            }
        );

        // TODO use this in generate-diff.yml
        tasks.register(
            DiffDirectoriesTask.GENERATE_DIFF_TASK_NAME,
            DiffDirectoriesTask.class,
            task -> {
                task.getAdditionalArgs().add("-bur");

                task.getFirst().convention(decompileTargetVineflower.flatMap(DecompileTargetVineflowerTask::getOutput));

                task.getSecond().convention(decompileVineflower.flatMap(DecompileVineflowerTask::getOutput));

                task.getDest().convention(this.getBuildDir().file("target.diff"));
            }
        );
    }

    public Provider<Directory> getTargetsBuildDir() {
        return this.getBuildDir().dir("targets");
    }

    public Provider<RegularFile> provideQuiltTargetBuildFile(Provider<String> version, String pathSuffix) {
        return this.getTargetsBuildDir().zip(version.map(createQuiltFileNameBuilder(pathSuffix)), Directory::file);
    }

    public Provider<RegularFile> provideQuiltTargetBuildFile(
        Provider<String> version, String classifier, String extension
    ) {
        return this.provideQuiltTargetBuildFile(version, "-" + classifier + "." + extension);
    }

    public Provider<RegularFile> provideQuiltTargetBuildJarFile(Provider<String> version, String classifier) {
        return this.provideQuiltTargetBuildFile(version, classifier, Jar.DEFAULT_EXTENSION);
    }

    public Provider<Directory> provideQuiltTargetBuildDir(Provider<String> version, String pathSuffix) {
        return this.getTargetsBuildDir().zip(version.map(createQuiltFileNameBuilder(pathSuffix)), Directory::dir);
    }

    public static Provider<Boolean> provideUnpickVersionsMatch(
        String unpickVersion, Provider<RegularFile> unpickMeta
    ) {
        return unpickMeta
            .map(meta -> {
                final JsonElement parsed;
                try (final var reader = new FileReader(meta.getAsFile())) {
                    parsed = JsonParser.parseReader(reader);
                } catch (IOException e) {
                    throw new GradleException("Failed to read unpick meta", e);
                }

                return parsed.getAsJsonObject().get("unpickVersion").getAsString();
            })
            .map(targetVersion -> targetVersion.equals(unpickVersion))
            .orElse(false);
    }

    private static Transformer<String, String> createQuiltFileNameBuilder(String pathSuffix) {
        return version -> Constants.MAPPINGS_NAME + "-" + version + pathSuffix;
    }
}
