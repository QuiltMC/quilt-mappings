package quilt.internal;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ResolveException;
import org.gradle.api.artifacts.VersionCatalogsExtension;
import org.gradle.api.artifacts.VersionConstraint;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.services.BuildServiceRegistry;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.enigma.api.service.JarIndexerService;
import quilt.internal.tasks.EnigmaProfileConsumingTask;
import quilt.internal.tasks.MappingsDirConsumingTask;
import quilt.internal.tasks.VersionDownloadInfoConsumingTask;
import quilt.internal.tasks.build.AddProposedMappingsTask;
import quilt.internal.tasks.build.BuildIntermediaryTask;
import quilt.internal.tasks.build.BuildMappingsTinyTask;
import quilt.internal.tasks.build.CompressTinyTask;
import quilt.internal.tasks.build.DropInvalidMappingsTask;
import quilt.internal.tasks.build.EraseByteCodeTask;
import quilt.internal.tasks.build.GenFakeSourceTask;
import quilt.internal.tasks.build.GeneratePackageInfoMappingsTask;
import quilt.internal.tasks.build.IntermediaryMappingsV2JarTask;
import quilt.internal.tasks.build.InvertPerVersionMappingsTask;
import quilt.internal.tasks.build.MappingsV2JarTask;
import quilt.internal.tasks.build.MergeIntermediaryTask;
import quilt.internal.tasks.build.MergeTinyTask;
import quilt.internal.tasks.build.MergeTinyV2Task;
import quilt.internal.tasks.build.RemoveIntermediaryTask;
import quilt.internal.tasks.build.TinyJarTask;
import quilt.internal.tasks.decompile.DecompileVineflowerTask;
import quilt.internal.tasks.diff.DecompileTargetVineflowerTask;
import quilt.internal.tasks.diff.DownloadTargetMappingJarTask;
import quilt.internal.tasks.diff.DownloadTargetMetaFileTask;
import quilt.internal.tasks.diff.ExtractTargetMappingJarTask;
import quilt.internal.tasks.diff.RemapTargetMinecraftJarTask;
import quilt.internal.tasks.diff.RemapTargetUnpickDefinitionsTask;
import quilt.internal.tasks.diff.TargetVersionConsumingTask;
import quilt.internal.tasks.diff.UnpickTargetJarTask;
import quilt.internal.tasks.diff.UnpickVersionsMatchConsumingTask;
import quilt.internal.tasks.diff.UnpickVersionsMatchSource;
import quilt.internal.tasks.jarmapping.MapJarTask;
import quilt.internal.tasks.jarmapping.MapNamedJarTask;
import quilt.internal.tasks.jarmapping.MapPerVersionMappingsJarTask;
import quilt.internal.tasks.lint.Checker;
import quilt.internal.tasks.lint.DownloadDictionaryFileTask;
import quilt.internal.tasks.lint.FindDuplicateMappingFilesTask;
import quilt.internal.tasks.lint.MappingLintTask;
import quilt.internal.tasks.mappings.AbstractEnigmaMappingsTask;
import quilt.internal.tasks.mappings.EnigmaMappingsServerTask;
import quilt.internal.tasks.mappings.EnigmaMappingsTask;
import quilt.internal.tasks.mappings.MappingsDirOutputtingTask;
import quilt.internal.tasks.setup.ConstantsJarTask;
import quilt.internal.tasks.setup.DownloadMinecraftJarsTask;
import quilt.internal.tasks.setup.DownloadVersionsManifestTask;
import quilt.internal.tasks.setup.DownloadWantedVersionManifestTask;
import quilt.internal.tasks.setup.ExtractTinyIntermediaryMappingsTask;
import quilt.internal.tasks.setup.ExtractServerJarTask;
import quilt.internal.tasks.setup.ExtractTinyMappingsTask;
import quilt.internal.tasks.setup.IntermediaryDependantTask;
import quilt.internal.tasks.setup.JavadocJarTask;
import quilt.internal.tasks.setup.MergeJarsTask;
import quilt.internal.tasks.setup.DownloadMinecraftLibrariesTask;
import quilt.internal.tasks.setup.SourcesJarTask;
import quilt.internal.tasks.unpick.CombineUnpickDefinitionsTask;
import quilt.internal.tasks.unpick.RemapUnpickDefinitionsTask;
import quilt.internal.tasks.unpick.UnpickJarTask;
import quilt.internal.tasks.unpick.gen.OpenGlConstantUnpickGenTask;
import quilt.internal.tasks.unpick.gen.UnpickGenTask;
import quilt.internal.decompile.javadoc.MappingsJavadocProvider;
import quilt.internal.util.EnigmaProfileService;
import quilt.internal.util.VersionDownloadInfo;

import javax.inject.Inject;
import java.io.File;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static quilt.internal.util.ProviderUtil.toOptional;

import static org.quiltmc.enigma_plugin.Arguments.SIMPLE_TYPE_FIELD_NAMES_PATH;

/**
 * TODO javadoc, including every configureEach, every configuration
 */
public abstract class QuiltMappingsPlugin implements Plugin<Project> {
    public static final String ENIGMA_RUNTIME_CONFIGURATION_NAME = "enigmaRuntime";
    public static final String PER_VERSION_MAPPINGS_CONFIGURATION_NAME = Constants.PER_VERSION_MAPPINGS_NAME;
    public static final String INTERMEDIARY_MAPPINGS_CONFIGURATION_NAME = Constants.INTERMEDIARY_MAPPINGS_NAME;
    public static final String UNPICK_CONFIGURATION_NAME = Constants.UNPICK_NAME;
    public static final String JAVADOC_CLASSPATH_CONFIGURATION_NAME = "javadocClasspath";

    private static final String ENIGMA_SERVER_PROP_PREFIX = "enigma_server_";
    public static final String ENIGMA_SERVER_PORT_PROP =
        ENIGMA_SERVER_PROP_PREFIX + EnigmaMappingsServerTask.PORT_OPTION;
    public static final String ENIGMA_SERVER_PASSWORD_PROP =
        ENIGMA_SERVER_PROP_PREFIX + EnigmaMappingsServerTask.PASSWORD_OPTION;
    public static final String ENIGMA_SERVER_LOG_PROP =
        ENIGMA_SERVER_PROP_PREFIX + EnigmaMappingsServerTask.LOG_OPTION;
    public static final String ENIGMA_SERVER_ARGS_PROP = ENIGMA_SERVER_PROP_PREFIX + "args";

    private static final String MAPPINGS_NAME_PREFIX = Constants.MAPPINGS_NAME + "-";
    private static final String ARCHIVE_FILE_NAME_PREFIX = MAPPINGS_NAME_PREFIX + Constants.MAPPINGS_VERSION;

    @Inject
    public abstract ProviderFactory getProviders();

    @Inject
    public abstract ObjectFactory getObjects();

    @Override
    public void apply(@NotNull Project project) {
        final ProviderFactory providers = this.getProviders();
        final ObjectFactory objects = this.getObjects();

        final String unpickVersion = project.getExtensions().getByType(VersionCatalogsExtension.class)
            .named("libs")
            .findVersion(Constants.UNPICK_NAME)
            .map(VersionConstraint::getRequiredVersion)
            .orElseThrow(() -> new GradleException(
                """
                Could not find unpick version.
                \tAn 'unpick' version must be specified in the 'libs' version catalog,
                \tusually by adding it to 'gradle/libs.versions.toml'.
                """
            ));

        final ProjectLayout projectLayout = project.getLayout();

        final Directory projectDir = projectLayout.getProjectDirectory();
        // TODO is it important that this is in .gradle/ instead of build/?
        //  It means it doesn't get cleaned, and idk how to retrieve the configured gradle project cache dir
        final Directory targetsDir = projectDir.dir(".gradle/targets");

        final DirectoryProperty buildDir = projectLayout.getBuildDirectory();
        final Provider<Directory> mappingsDir = buildDir.dir("mappings");
        final Provider<Directory> libsDir = buildDir.dir("libs");
        final Provider<Directory> minecraftDir = buildDir.dir("minecraft");
        final Provider<Directory> tempDir = buildDir.dir("temp");

        final BiFunction<String, String, Provider<RegularFile>> provideMappingsDest = (mappingsName, fileExt) -> {
            return minecraftDir.map(dir ->
                dir.file(Constants.MINECRAFT_VERSION + "-" + mappingsName + "." + fileExt)
            );
        };

        // adds tasks: javadoc, jar, check, build
        project.getPluginManager().apply(JavaPlugin.class);

        final ExtensionContainer extensions = project.getExtensions();

        final var ext = extensions.create(QuiltMappingsExtension.EXTENSION_NAME, QuiltMappingsExtension.class);

        final ConfigurationContainer configurations = project.getConfigurations();

        final Configuration enigmaRuntime = configurations.create(ENIGMA_RUNTIME_CONFIGURATION_NAME);
        final Configuration perVersionMappings = configurations.create(PER_VERSION_MAPPINGS_CONFIGURATION_NAME);
        final Configuration intermediaryMappings = configurations.create(INTERMEDIARY_MAPPINGS_CONFIGURATION_NAME);
        final Configuration unpick = configurations.create(UNPICK_CONFIGURATION_NAME);
        final Configuration javadocClasspath = configurations.create(JAVADOC_CLASSPATH_CONFIGURATION_NAME);

        final BuildServiceRegistry services = project.getGradle().getSharedServices();

        final var enigmaProfile = services.registerIfAbsent(
            EnigmaProfileService.ENIGMA_PROFILE_SERVICE_NAME,
            EnigmaProfileService.class,
            spec -> spec.parameters(params -> {
                params.getProfileConfig().convention(ext.getEnigmaProfileConfig());
            })
        );

        final TaskContainer tasks = project.getTasks();

        {
            // save this in a property so all tasks use the same cached value
            final var simpleTypeFieldNamePaths = objects.listProperty(String.class);
            simpleTypeFieldNamePaths.set(
                enigmaProfile
                    .map(EnigmaProfileService::getProfile)
                    .map(profile ->
                        profile.getServiceProfiles(JarIndexerService.TYPE).stream()
                            .flatMap(service -> service.getArgument(SIMPLE_TYPE_FIELD_NAMES_PATH).stream())
                            .map(stringOrStrings -> stringOrStrings.mapBoth(Stream::of, Collection::stream))
                            .flatMap(bothStringStreams ->
                                bothStringStreams.left().orElseGet(bothStringStreams::rightOrThrow)
                            )
                            .toList()
                    )
            );

            tasks.withType(EnigmaProfileConsumingTask.class).configureEach(task -> {
                task.getEnigmaProfileService().convention(enigmaProfile);

                task.getEnigmaProfileConfig().convention(ext.getEnigmaProfileConfig());

                task.getSimpleTypeFieldNamesFiles().from(simpleTypeFieldNamePaths);
            });
        }

        this.provideDefaultError(
            ext.getEnigmaProfileConfig(),
            "No enigma profile specified. " +
                "A profile must be specified to use an " + EnigmaProfileConsumingTask.class.getSimpleName() + "."
        );

        {
            final var mappingsDirOutputtingTasks = tasks.withType(MappingsDirOutputtingTask.class);

            mappingsDirOutputtingTasks.configureEach(task -> {
                task.getMappingsDir().convention(ext.getMappingsDir());
            });

            tasks.withType(MappingsDirConsumingTask.class).configureEach(task -> {
                task.getMappingsDir().convention(ext.getMappingsDir());
                task.getInputs().files(mappingsDirOutputtingTasks);
            });
        }

        this.provideDefaultError(
            ext.getMappingsDir(),
            "No mappings directory specified. " +
                "A directory must be specified to use a " + MappingsDirConsumingTask.class.getSimpleName() + "."
        );

        final var downloadVersionsManifest = tasks.register(
            DownloadVersionsManifestTask.DOWNLOAD_VERSIONS_MANIFEST_TASK_NAME,
            DownloadVersionsManifestTask.class,
            task -> {
                task.getDest().convention(minecraftDir.map(dir -> dir.file("version_manifest_v2.json")));
            }
        );

        {
            final var downloadWantedVersionManifest = tasks.register(
                DownloadWantedVersionManifestTask.DOWNLOAD_WANTED_VERSION_MANIFEST_TASK_NAME,
                DownloadWantedVersionManifestTask.class,
                task -> {
                    task.getManifestVersion().convention(
                        downloadVersionsManifest.flatMap(DownloadVersionsManifestTask::provideVersionEntry)
                    );

                    task.getDest().convention(
                        minecraftDir.map(dir -> dir.file(Constants.MINECRAFT_VERSION + ".json"))
                    );
                }
            );

            // put mapped provider in a property so all tasks use the same cached value
            final Provider<VersionDownloadInfo> versionDownloadInfo =
                objects.property(VersionDownloadInfo.class).convention(
                    downloadWantedVersionManifest.flatMap(DownloadWantedVersionManifestTask::provideVersionDownloadInfo)
                );

            tasks.withType(VersionDownloadInfoConsumingTask.class).configureEach(task -> {
                task.getVersionDownloadInfo().convention(versionDownloadInfo);
            });
        }

        final var downloadMinecraftJars = tasks.register(
            DownloadMinecraftJarsTask.DOWNLOAD_MINECRAFT_JARS_TASK_NAME,
            DownloadMinecraftJarsTask.class,
            task -> {
                task.getClientJar().convention(
                    minecraftDir.map(dir -> dir.file(Constants.MINECRAFT_VERSION + "-client.jar"))
                );

                task.getServerBootstrapJar().convention(
                    minecraftDir.map(dir -> dir.file(Constants.MINECRAFT_VERSION + "-server-bootstrap.jar"))
                );
            }
        );

        final var extractServerJar = tasks.register(
            ExtractServerJarTask.EXTRACT_SERVER_JAR_TASK_NAME,
            ExtractServerJarTask.class,
            task -> {
                task.getZippedFile().convention(
                    downloadMinecraftJars.flatMap(DownloadMinecraftJarsTask::getServerBootstrapJar)
                );

                task.getExtractionDest().convention(
                    minecraftDir.map(dir -> dir.file(Constants.MINECRAFT_VERSION + "-server.jar"))
                );
            }
        );

        final var mergeJars = tasks.register(
            MergeJarsTask.MERGE_JARS_TASK_NAME,
            MergeJarsTask.class,
            task -> {
                task.getClientJar().convention(downloadMinecraftJars.flatMap(DownloadMinecraftJarsTask::getClientJar));

                task.getServerJar().convention(extractServerJar.flatMap(ExtractServerJarTask::getExtractionDest));

                // TODO move this and other jars that are directly in the project dir to some sub dir
                task.getMergedFile().convention(projectDir.file(Constants.MINECRAFT_VERSION + "-merged.jar"));
            }
        );

        final var downloadMinecraftLibraries = tasks.register(
            DownloadMinecraftLibrariesTask.DOWNLOAD_MINECRAFT_LIBRARIES_TASK_NAME,
            DownloadMinecraftLibrariesTask.class,
            task -> {
                task.getLibrariesDir().convention(minecraftDir.map(dir -> dir.dir("libraries")));
            }
        );

        tasks.withType(MapJarTask.class).configureEach(task -> {
            task.getLibrariesDir().convention(
                downloadMinecraftLibraries.flatMap(DownloadMinecraftLibrariesTask::getLibrariesDir)
            );
        });

        final var extractTinyPerVersionMappings = tasks.register(
            ExtractTinyMappingsTask.EXTRACT_TINY_PER_VERSION_MAPPINGS_TASK_NAME,
            ExtractTinyMappingsTask.class,
            task -> {
                task.getZippedFile().convention(this.provideRequiredFile(perVersionMappings));

                task.getExtractionDest().convention(
                    provideMappingsDest.apply(Constants.PER_VERSION_MAPPINGS_NAME, "tiny")
                );
            }
        );

        final var invertPerVersionMappings = tasks.register(
            InvertPerVersionMappingsTask.INVERT_PER_VERSION_MAPPINGS_TASK_NAME,
            InvertPerVersionMappingsTask.class,
            task -> {
                task.getInput().convention(
                    extractTinyPerVersionMappings.flatMap(ExtractTinyMappingsTask::getExtractionDest)
                );

                task.getInvertedTinyFile().convention(
                    provideMappingsDest.apply(Constants.PER_VERSION_INVERTED_MAPPINGS_NAME, "tiny")
                );
            }
        );

        final var mapPerVersionMappingsJar = tasks.register(
            MapPerVersionMappingsJarTask.MAP_PER_VERSION_MAPPINGS_JAR_TASK_NAME,
            MapPerVersionMappingsJarTask.class,
            task -> {
                task.getInputJar().convention(mergeJars.flatMap(MergeJarsTask::getMergedFile));

                task.getMappingsFile().convention(
                    extractTinyPerVersionMappings.flatMap(ExtractTinyMappingsTask::getExtractionDest)
                );

                // TODO move this and other jars that are directly in the project dir to some sub dir
                task.getOutputJar().convention(projectDir.file(
                    Constants.MINECRAFT_VERSION + "-" + Constants.PER_VERSION_MAPPINGS_NAME + ".jar"
                ));
            }
        );

        final var buildMappingsTiny = tasks.register(
            BuildMappingsTinyTask.BUILD_MAPPINGS_TINY_TASK_NAME,
            BuildMappingsTinyTask.class,
            task -> {
                task.getPerVersionMappingsJar().convention(
                    mapPerVersionMappingsJar.flatMap(MapPerVersionMappingsJarTask::getOutputJar)
                );

                task.getOutputMappings().convention(
                    mappingsDir.map(dir -> dir.file(Constants.MAPPINGS_NAME + ".tiny"))
                );
            }
        );

        final var insertAutoGeneratedMappings = tasks.register(
            AddProposedMappingsTask.INSERT_AUTO_GENERATED_MAPPINGS_TASK_NAME,
            AddProposedMappingsTask.class,
            task -> {
                task.getInputJar().convention(
                    mapPerVersionMappingsJar.flatMap(MapPerVersionMappingsJarTask::getOutputJar)
                );

                task.getInputMappings().convention(buildMappingsTiny.flatMap(BuildMappingsTinyTask::getOutputMappings));

                task.getOutputMappings().convention(
                    mappingsDir.zip(task.getInputMappings(), (dir, input) ->
                        dir.file(getNameWithExtension(input, "-inserted.tiny"))
                    )
                );

                task.getPreprocessedMappings().convention(
                    tempDir.zip(task.getInputMappings(), (dir, input) ->
                        dir.file(getNameWithExtension(input, "-preprocessed.tiny"))
                    )
                );

                task.getProcessedMappings().convention(
                    tempDir.zip(task.getInputMappings(), (dir, input) ->
                        dir.file(getNameWithExtension(input, "-processed.tiny"))
                    )
                );
            }
        );

        final var mergeTiny = tasks.register(
            MergeTinyTask.MERGE_TINY_TASK_NAME,
            MergeTinyTask.class,
            task -> {
                task.getInput().convention(buildMappingsTiny.flatMap(BuildMappingsTinyTask::getOutputMappings));

                task.getHashedTinyMappings().convention(
                    invertPerVersionMappings.flatMap(InvertPerVersionMappingsTask::getInvertedTinyFile)
                );

                task.getOutputMappings().convention(mappingsDir.map(dir -> dir.file("mappings.tiny")));
            }
        );

        final var tinyJar = tasks.register(
            TinyJarTask.TINY_JAR_TASK_NAME,
            TinyJarTask.class,
            task -> {
                task.getMappings().convention(mergeTiny.flatMap(MergeTinyTask::getOutputMappings));

                task.getArchiveFileName().convention(ARCHIVE_FILE_NAME_PREFIX + ".jar");

                task.getDestinationDirectory().convention(libsDir);
            }
        );

        // Its artifact collides with the `tinyJar` one, just disable it since it isn't used either way
        tasks.named(JavaPlugin.JAR_TASK_NAME).configure(task -> task.setEnabled(false));

        tasks.register(
            CompressTinyTask.COMPRESS_TINY_TASK_NAME,
            CompressTinyTask.class,
            task -> {
                task.getMappings().convention(mergeTiny.flatMap(MergeTinyTask::getOutputMappings));

                task.getCompressedTiny().convention(
                    tinyJar.flatMap(TinyJarTask::getDestinationDirectory)
                        .map(dir -> dir.file(ARCHIVE_FILE_NAME_PREFIX + "-tiny.gz"))
                );
            }
        );

        tasks.register(
            DropInvalidMappingsTask.DROP_INVALID_MAPPINGS_TASK_NAME,
            DropInvalidMappingsTask.class,
            task -> {
                task.getPerVersionMappingsJar().convention(
                    mapPerVersionMappingsJar.flatMap(MapPerVersionMappingsJarTask::getOutputJar)
                );
            }
        );

        tasks.register(
            OpenGlConstantUnpickGenTask.OPEN_GL_UNPICK_GEN_TASK_NAME,
            OpenGlConstantUnpickGenTask.class,
            task -> {
                task.getPerVersionMappingsJar().convention(
                    mapPerVersionMappingsJar.flatMap(MapPerVersionMappingsJarTask::getOutputJar)
                );

                task.getArtifactsByName().convention(
                    downloadMinecraftLibraries.flatMap(DownloadMinecraftLibrariesTask::getArtifactsByName)
                );

                task.getUnpickGlStateManagerDefinitions().convention(
                    mappingsDir.map(dir -> dir.file("unpick_glstatemanager.unpick"))
                );

                task.getUnpickGlDefinitions().convention(
                    mappingsDir.map(dir -> dir.file("unpick_gl.unpick"))
                );
            }
        );

        final var combineUnpickDefinitions = tasks.register(
            CombineUnpickDefinitionsTask.COMBINE_UNPICK_DEFINITIONS_TASK_NAME,
            CombineUnpickDefinitionsTask.class,
            task -> {
                task.getUnpickDefinitions().from(project.getTasks().withType(UnpickGenTask.class));

                task.getOutput().convention(
                    mappingsDir.map(dir -> dir.file("definitions.unpick"))
                );
            }
        );

        // constants are configured in build.gradle because they're' from a project source set
        final var constantsJar = tasks.register(ConstantsJarTask.CONSTANTS_JAR_TASK_NAME, ConstantsJarTask.class);

        tasks.register(
            GeneratePackageInfoMappingsTask.GENERATE_PACKAGE_INFO_MAPPINGS_TASK_NAME,
            GeneratePackageInfoMappingsTask.class,
            task -> {
                task.getInputJar().convention(mapPerVersionMappingsJar.flatMap(MapPerVersionMappingsJarTask::getOutputJar));
            }
        );

        final var downloadDictionaryFile = tasks.register(
            DownloadDictionaryFileTask.DOWNLOAD_DICTIONARY_FILE_TASK_NAME,
            DownloadDictionaryFileTask.class,
            task -> {
                // configuration is in build.gradle because it depends on an external url that is prone to change
                // the output file configuration could be moved here if its name didn't contain the revision

                this.provideDefaultError(
                    task.getUrl(),
                    "No url specified. " +
                        "A url must be specified to use " + task.getName() + " or any task that depends on it."
                );

                this.provideDefaultError(
                    task.getDest(),
                    "No dest specified." +
                        "An dest must be specified to use " + task.getName() + " or any task that depends on it."
                );
            }
        );

        final var findDuplicateMappingFiles = tasks.register(
            FindDuplicateMappingFilesTask.FIND_DUPLICATE_MAPPING_FILES_TASK_NAME,
            FindDuplicateMappingFilesTask.class
        );

        final var mappingLint = tasks.register(
            MappingLintTask.MAPPING_LINT_TASK_NAME,
            MappingLintTask.class,
            task -> {
                // this does mappings verification but has no output to depend on
                task.dependsOn(findDuplicateMappingFiles);

                task.getJarFile().convention(
                    mapPerVersionMappingsJar.flatMap(MapPerVersionMappingsJarTask::getOutputJar)
                );

                task.getCheckers().addAll(Checker.DEFAULT_CHECKERS);

                task.getDictionaryFile().convention(
                    downloadDictionaryFile.flatMap(DownloadDictionaryFileTask::getDest)
                );
            }
        );

        tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure(task -> task.dependsOn(mappingLint));

        final var mergeTinyV2 = tasks.register(
            MergeTinyV2Task.MERGE_TINY_V_2_TASK_NAME,
            MergeTinyV2Task.class,
            task -> {
                // TODO this used to be dependent on v2UnmergedMappingsJar, but afaict it has no effect on this task

                task.getInput().convention(
                    insertAutoGeneratedMappings.flatMap(AddProposedMappingsTask::getOutputMappings)
                );

                task.getHashedTinyMappings().convention(
                    invertPerVersionMappings.flatMap(InvertPerVersionMappingsTask::getInvertedTinyFile)
                );

                task.getOutputMappings().convention(
                    mappingsDir.map(dir -> dir.file("merged2.tiny"))
                );
            }
        );

        final var extractTinyIntermediaryMappings = tasks.register(
            ExtractTinyIntermediaryMappingsTask.EXTRACT_TINY_INTERMEDIARY_MAPPINGS_TASK_NAME,
            ExtractTinyIntermediaryMappingsTask.class,
            task -> {
                task.getExtractionDest().convention(
                    provideMappingsDest.apply(Constants.INTERMEDIARY_MAPPINGS_NAME, "tiny")
                );
            }
        );

        {
            final Provider<RegularFile> intermediaryFile = this.provideOptionalFile(intermediaryMappings);

            tasks.withType(IntermediaryDependantTask.class).configureEach(task -> {
                task.onlyIf(unused -> intermediaryFile.isPresent());
            });

            extractTinyIntermediaryMappings.configure(task -> {
                task.getZippedFile().convention(intermediaryFile);
            });
        }

        final var mergeIntermediary = tasks.register(
            MergeIntermediaryTask.MERGE_INTERMEDIARY_TASK_NAME,
            MergeIntermediaryTask.class,
            task -> {
                task.getInput().convention(
                    extractTinyIntermediaryMappings.flatMap(ExtractTinyMappingsTask::getExtractionDest)
                );

                task.getMergedTinyMappings().convention(mergeTinyV2.flatMap(MergeTinyV2Task::getOutputMappings));

                task.getOutputMappings().convention(
                    mappingsDir.map(dir -> dir.file("mappings-intermediaryMerged.tiny"))
                );
            }
        );

        final var removeIntermediary = tasks.register(
            RemoveIntermediaryTask.REMOVE_INTERMEDIARY_TASK_NAME,
            RemoveIntermediaryTask.class,
            task -> {
                task.getInput().convention(mergeIntermediary.flatMap(MergeIntermediaryTask::getOutputMappings));

                task.getOutputMappings().convention(
                    mappingsDir.map(dir -> dir.file("mappings-intermediary.tiny"))
                );
            }
        );

        tasks.withType(MappingsV2JarTask.class).configureEach(task -> {
            task.getUnpickMeta().convention(ext.getUnpickMeta());

            task.getUnpickDefinition().convention(
                combineUnpickDefinitions.flatMap(CombineUnpickDefinitionsTask::getOutput)
            );

            task.getDestinationDirectory().convention(libsDir);
        });

        {
            final var v2UnmergedMappingsJar = tasks.register(
                MappingsV2JarTask.V_2_UNMERGED_MAPPINGS_JAR_TASK_NAME,
                MappingsV2JarTask.class,
                unpickVersion
            );
            v2UnmergedMappingsJar.configure(task -> {
                task.getMappings().convention(
                    insertAutoGeneratedMappings.flatMap(AddProposedMappingsTask::getOutputMappings)
                );

                task.getArchiveFileName().convention(ARCHIVE_FILE_NAME_PREFIX + "-v2.jar");
            });
        }

        final var intermediaryV2MappingsJar = tasks.register(
            IntermediaryMappingsV2JarTask.INTERMEDIARY_V_2_MAPPINGS_JAR_TASK_NAME,
            IntermediaryMappingsV2JarTask.class,
            unpickVersion
        );
        intermediaryV2MappingsJar.configure(task -> {
            task.getMappings().convention(removeIntermediary.flatMap(RemoveIntermediaryTask::getOutputMappings));

            task.getArchiveFileName().convention(
                ARCHIVE_FILE_NAME_PREFIX + "-" + IntermediaryMappingsV2JarTask.CLASSIFIER + ".jar"
            );
        });

        {
            final var v2MergedMappingsJar = tasks.register(
                MappingsV2JarTask.V_2_MERGED_MAPPINGS_JAR_TASK_NAME,
                MappingsV2JarTask.class,
                unpickVersion
            );
            v2MergedMappingsJar.configure(task -> {
                task.getMappings().convention(mergeTinyV2.flatMap(MergeTinyV2Task::getOutputMappings));

                task.getArchiveFileName().convention(ARCHIVE_FILE_NAME_PREFIX + "-mergedv2.jar");
            });
        }

        final var remapUnpickDefinitions = tasks.register(
            RemapUnpickDefinitionsTask.REMAP_UNPICK_DEFINITIONS_TASK_NAME,
            RemapUnpickDefinitionsTask.class,
            task -> {
                task.getInput().convention(combineUnpickDefinitions.flatMap(CombineUnpickDefinitionsTask::getOutput));

                task.getMappings().convention(mergeTinyV2.flatMap(MergeTinyV2Task::getOutputMappings));

                task.getOutput().convention(mappingsDir.map(dir ->
                    dir.file(Constants.PER_VERSION_MAPPINGS_NAME + "-definitions.unpick")
                ));
            }
        );

        tasks.withType(UnpickJarTask.class).configureEach(task -> {
            task.classpath(unpick);

            task.getInputFile().convention(
                mapPerVersionMappingsJar.flatMap(MapPerVersionMappingsJarTask::getOutputJar)
            );

            task.getDecompileClasspathFiles().from(
                downloadMinecraftLibraries.flatMap(DownloadMinecraftLibrariesTask::getLibrariesDir)
                    .map(Directory::getAsFileTree)
                    .map(FileTree::getFiles)
            );
        });

        final var unpickHashedJar = tasks.register(
            UnpickJarTask.UNPICK_HASHED_JAR_TASK_NAME,
            UnpickJarTask.class,
            task -> {
                task.getUnpickDefinition().convention(
                    remapUnpickDefinitions.flatMap(RemapUnpickDefinitionsTask::getOutput)
                );

                task.getUnpickConstantsJar().set(constantsJar.flatMap(ConstantsJarTask::getArchiveFile));

                // TODO move this and other jars that are directly in the project dir to some sub dir
                task.getOutputFile().convention(projectDir.file(
                    Constants.MINECRAFT_VERSION + "-" + Constants.PER_VERSION_MAPPINGS_NAME + "-unpicked.jar"
                ));
            }
        );

        final var mapNamedJar = tasks.register(
            MapNamedJarTask.MAP_NAMED_JAR_TASK_NAME,
            MapNamedJarTask.class,
            task -> {
                task.getInputJar().convention(unpickHashedJar.flatMap(UnpickJarTask::getOutputFile));

                task.getMappingsFile().convention(
                    insertAutoGeneratedMappings.flatMap(AddProposedMappingsTask::getOutputMappings)
                );

                task.getOutputJar().convention(projectDir.file(Constants.MINECRAFT_VERSION + "-named.jar"));
            }
        );

        tasks.withType(AbstractEnigmaMappingsTask.class).configureEach(task -> {
            task.classpath(enigmaRuntime);

            task.jvmArgs("-Xmx2048m");
        });

        tasks.register(
            EnigmaMappingsTask.MAPPINGS_UNPICKED_TASK_NAME,
            EnigmaMappingsTask.class,
            task -> {
                task.getJarToMap().convention(unpickHashedJar.flatMap(UnpickJarTask::getOutputFile));
            }
        );

        tasks.register(
            EnigmaMappingsTask.MAPPINGS_TASK_NAME,
            EnigmaMappingsTask.class,
            task -> {
                task.getJarToMap().convention(
                    mapPerVersionMappingsJar.flatMap(MapPerVersionMappingsJarTask::getOutputJar)
                );
            }
        );

        tasks.withType(EnigmaMappingsServerTask.class).configureEach(task -> {
            task.getPort().convention(
                providers.gradleProperty(ENIGMA_SERVER_PORT_PROP)
            );

            task.getPassword().convention(
                providers.gradleProperty(ENIGMA_SERVER_PASSWORD_PROP)
            );

            task.getLog().convention(
                providers.gradleProperty(ENIGMA_SERVER_LOG_PROP)
                    .map(projectDir::file)
                    .orElse(buildDir.file("logs/server.log"))
            );

            toOptional(
                providers.gradleProperty(ENIGMA_SERVER_ARGS_PROP).map(args -> args.split(" "))
            ).ifPresent(task::args);
        });

        tasks.register(
            EnigmaMappingsServerTask.MAPPINGS_UNPICKED_SERVER_TASK_NAME,
            EnigmaMappingsServerTask.class,
            task -> {
                task.getJarToMap().convention(unpickHashedJar.flatMap(UnpickJarTask::getOutputFile));
            }
        );

        tasks.register(
            EnigmaMappingsServerTask.MAPPINGS_SERVER_TASK_NAME,
            EnigmaMappingsServerTask.class,
            task -> {
                task.getJarToMap().convention(mapPerVersionMappingsJar.flatMap(MapJarTask::getOutputJar));
            }
        );

        final var intermediaryV2MergedMappingsJar = tasks.register(
            IntermediaryMappingsV2JarTask.INTERMEDIARY_V_2_MERGED_MAPPINGS_JAR_TASK_NAME,
            IntermediaryMappingsV2JarTask.class,
            unpickVersion
        );
        intermediaryV2MergedMappingsJar.configure(task -> {
            task.getMappings().convention(mergeIntermediary.flatMap(MergeIntermediaryTask::getOutputMappings));

            task.getArchiveFileName().convention(
                ARCHIVE_FILE_NAME_PREFIX + "-" + IntermediaryMappingsV2JarTask.MERGED_CLASSIFIER + ".jar"
            );
        });

        final var eraseBytecode = tasks.register(
            EraseByteCodeTask.ERASE_BYTECODE_TASK_NAME,
            EraseByteCodeTask.class,
            task -> {
                task.getJarFile().convention(mapNamedJar.flatMap(MapNamedJarTask::getOutputJar));

                task.getOutput().convention(tempDir.map(dir -> dir.dir("erased-classes")));
            }
        );

        final var genFakeSource = tasks.register(
            GenFakeSourceTask.GEN_FAKE_SOURCE_TASK_NAME,
            GenFakeSourceTask.class,
            task -> {
                task.getSources().from(eraseBytecode.flatMap(EraseByteCodeTask::getOutput));

                task.getLibraries().from(
                    downloadMinecraftLibraries.flatMap(DownloadMinecraftLibrariesTask::getLibrariesDir)
                );

                task.getDefaultJavadocSource().convention(providers.of(
                    MappingsJavadocProvider.Source.class,
                    spec -> spec.parameters(params -> {
                        params.getMappingsFile().set(mergeTinyV2.flatMap(MergeTinyV2Task::getOutputMappings));

                        params.getNamespace().set("named");
                    })
                ));

                task.getOutput().convention(tempDir.map(dir -> dir.dir("fakeSource")));
            }
        );

        tasks.register(
            DecompileVineflowerTask.DECOMPILE_VINEFLOWER_TASK_NAME,
            DecompileVineflowerTask.class,
            task -> {
                task.getSources().from(mapNamedJar.flatMap(MapNamedJarTask::getOutputJar));

                task.getLibraries().from(
                    downloadMinecraftLibraries.flatMap(DownloadMinecraftLibrariesTask::getLibrariesDir)
                );

                task.getDefaultJavadocSource().convention(providers.of(
                    MappingsJavadocProvider.Source.class,
                    spec -> spec.parameters(params -> {
                        params.getMappingsFile().set(
                            insertAutoGeneratedMappings.flatMap(AddProposedMappingsTask::getOutputMappings)
                        );

                        params.getNamespace().set("named");
                    })
                ));

                // TODO move this once generateDiff task eliminates magic strings
                task.getOutput().convention(projectDir.dir("namedSrc"));
            }
        );

        tasks.register(
            BuildIntermediaryTask.BUILD_INTERMEDIARY_TASK_NAME,
            BuildIntermediaryTask.class,
            task -> {
                task.dependsOn(intermediaryV2MappingsJar, intermediaryV2MergedMappingsJar);
            }
        );

        {
            final var downloadTargetMetaFile = tasks.register(
                DownloadTargetMetaFileTask.DOWNLOAD_TARGET_META_FILE_TASK_NAME,
                DownloadTargetMetaFileTask.class,
                task -> {
                    task.getDest().convention(minecraftDir.map(dir ->
                        dir.file(MAPPINGS_NAME_PREFIX + Constants.MINECRAFT_VERSION + ".json")
                    ));
                }
            );

            // put mapped provider in a property so all tasks use the same cached value
            final Provider<String> targetVersion = objects.property(String.class).convention(
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
                task.getTargetUnpickConstantsFile().convention(task.provideVersionedFile(
                    targetsDir,
                    version -> MAPPINGS_NAME_PREFIX + version + "-constants.jar"
                ));

                task.getTargetJar().convention(task.provideVersionedFile(
                    targetsDir,
                    version -> MAPPINGS_NAME_PREFIX + version + "-v2.jar"
                ));
            }
        );

        final var extractTargetMappingsJar = tasks.register(
            ExtractTargetMappingJarTask.EXTRACT_TARGET_MAPPINGS_JAR_TASK_NAME,
            ExtractTargetMappingJarTask.class,
            task -> {
                task.getZippedFile().convention(
                    downloadTargetMappingsJar.flatMap(DownloadTargetMappingJarTask::getTargetJar)
                );

                task.getExtractionDest().convention(task.provideVersionedDir(
                    targetsDir,
                    version -> MAPPINGS_NAME_PREFIX + version
                ));
            }
        );

        final Provider<Boolean> unpickVersionsMatch = providers.of(
            UnpickVersionsMatchSource.class,
            spec -> spec.parameters(params -> {
                params.getUnpickVersion().convention(unpickVersion);

                params.getUnpickMeta().convention(
                    extractTargetMappingsJar.flatMap(ExtractTargetMappingJarTask::getExtractionDest)
                        .map(dest -> dest.file(MappingsV2JarTask.JAR_UNPICK_META_PATH))
                );
            })
        );

        tasks.withType(UnpickVersionsMatchConsumingTask.class).configureEach(task -> {
            task.getUnpickVersionsMatch().convention(unpickVersionsMatch);

            task.onlyIf(unused -> task.getUnpickVersionsMatch().get());
        });

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
                        .map(dest -> dest.file(MappingsV2JarTask.JAR_MAPPINGS_PATH))
                );

                task.getOutput().convention(task.provideVersionedFile(
                    targetsDir,
                    version -> MAPPINGS_NAME_PREFIX + version + "remapped-unpick.unpick"
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

                task.getOutputFile().convention(task.provideVersionedFile(
                    targetsDir,
                    version -> MAPPINGS_NAME_PREFIX + version + "-unpicked.jar"
                ));
            }
        );

        final var remapTargetMinecraftJar = tasks.register(
            RemapTargetMinecraftJarTask.REMAP_TARGET_MINECRAFT_JAR_TASK_NAME,
            RemapTargetMinecraftJarTask.class,
            task -> {
                task.getInputJar().convention(unpickTargetJar.flatMap(UnpickTargetJarTask::getOutputFile));

                task.getMappingsFile().convention(
                    extractTargetMappingsJar.flatMap(ExtractTargetMappingJarTask::getExtractionDest)
                        .map(dest -> dest.dir("mappings").file("mappings.tiny"))
                );

                task.getOutputJar().convention(task.provideVersionedFile(
                    targetsDir,
                    version -> MAPPINGS_NAME_PREFIX + version + "-named.jar"
                ));
            }
        );

        tasks.register(
            DecompileTargetVineflowerTask.DECOMPILE_TARGET_VINEFLOWER_TASK_NAME,
            DecompileTargetVineflowerTask.class,
            task -> {
                task.getSources().from(remapTargetMinecraftJar.flatMap(RemapTargetMinecraftJarTask::getOutputJar));

                task.getLibraries().from(
                    downloadMinecraftLibraries.flatMap(DownloadMinecraftLibrariesTask::getLibrariesDir)
                );

                task.getDefaultJavadocSource().convention(providers.of(
                    MappingsJavadocProvider.Source.class,
                    spec -> spec.parameters(params -> {
                        params.getMappingsFile().convention(
                            extractTargetMappingsJar.flatMap(ExtractTargetMappingJarTask::getExtractionDest)
                                .map(dest -> dest.dir("mappings").file("mappings.tiny"))
                        );

                        params.getNamespace().convention("named");
                    })
                )
            );

            // TODO move this once generateDiff task eliminates magic strings
            task.getOutput().convention(projectDir.dir("namedTargetSrc"));
        });

        // TODO add generateDiff task,
        //  allow passing its output location on command line and pass it in generate-diff.yml

        // sources are added in build.gradle because they're from a project source set
        tasks.register(SourcesJarTask.SOURCES_JAR_TASK_NAME, SourcesJarTask.class);

        final var javadoc = tasks.named(JavaPlugin.JAVADOC_TASK_NAME, Javadoc.class, task -> {
            task.setGroup(Constants.Groups.JAVADOC_GENERATION);

            task.source(genFakeSource.flatMap(GenFakeSourceTask::getOutput).map(Directory::getAsFileTree));

            task.setClasspath(
                projectDir.files(
                    javadocClasspath,
                    downloadMinecraftLibraries
                        .flatMap(DownloadMinecraftLibrariesTask::getLibrariesDir)
                        .map(Directory::getAsFileTree),
                    mapNamedJar.flatMap(MapNamedJarTask::getOutputJar)
                )
            );
        });

        tasks.register(JavadocJarTask.JAVADOC_JAR_TASK_NAME, JavadocJarTask.class, task -> {
            task.from(javadoc.map(Javadoc::getDestinationDir));
        });
    }

    private void provideDefaultError(Property<?> property, String errorMessage) {
        property.convention(this.getProviders().provider(() -> { throw new GradleException(errorMessage); }));
    }

    private static String getNameWithExtension(RegularFile file, String extensionReplacement) {
        return replaceExtension(file.getAsFile().getName(), extensionReplacement);
    }

    private static String replaceExtension(String fileName, String replacement) {
        final String newName = FilenameUtils.removeExtension(fileName) + replacement;

        if (newName.equals(fileName)) {
            throw new IllegalArgumentException(
                """
                File extension replacement did not change name.
                \tfileName: "%s"
                \treplacement: "%s"
                """.formatted(fileName, replacement)
            );
        }

        return newName;
    }

    /**
     * Provides a single optional file from the passed {@code resolvableConfiguration}.
     *
     * @param resolvableConfiguration a {@link Configuration#isCanBeResolved() resolvable} {@link Configuration}
     *                               that should hold exactly one file
     *
     * @return a provider holding the contents of the passed {@code resolvableConfiguration} if
     * resolution succeeds and it contains exactly one file, or an empty provider if resolution fails
     *
     * @throws IllegalArgumentException if the passed {@code resolvableConfiguration} is not
     * {@link Configuration#isCanBeResolved() resolvable}
     * @throws IllegalStateException if the passed {@code resolvableConfiguration} doesn't contain exactly one file
     */
    public Provider<RegularFile> provideOptionalFile(Configuration resolvableConfiguration) {
        return this.provideFile(resolvableConfiguration, configuration -> {
            try {
                return resolvableConfiguration.getSingleFile();
            } catch (ResolveException e) {
                // returning null results in an empty provider
                return null;
            }
        });
    }

    /**
     * Provides a single required file from the passed {@code resolvableConfiguration}.
     *
     * @param resolvableConfiguration a {@link Configuration#isCanBeResolved() resolvable} {@link Configuration}
     *                               that should hold exactly one file
     *
     * @return a provider holding the contents of the passed {@link Configuration resolvableConfiguration}
     *
     * @throws IllegalArgumentException if the passed {@code resolvableConfiguration} is not
     * {@link Configuration#isCanBeResolved() resolvable}
     * @throws ResolveException if an error occurs in the resolution of the passed {@code resolvableConfiguration}
     * @throws IllegalStateException if the passed {@code resolvableConfiguration} doesn't contain exactly one file
     */
    public Provider<RegularFile> provideRequiredFile(Configuration resolvableConfiguration) {
        return this.provideFile(resolvableConfiguration, Configuration::getSingleFile);
    }

    protected Provider<RegularFile> provideFile(
        Configuration resolvableConfiguration, Function<Configuration, @Nullable File> resolver
    ) {
        return this.getProviders()
            .provider(() -> {
                if (!resolvableConfiguration.isCanBeResolved()) {
                    throw new IllegalArgumentException("The passed configuration must be resolvable");
                }

                return resolver.apply(resolvableConfiguration);
            })
            .flatMap(file -> {
                final RegularFileProperty regularFile = this.getObjects().fileProperty();
                regularFile.set(file);
                return regularFile;
            });
    }
}
