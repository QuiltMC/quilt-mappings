package quilt.internal.tasks.setup;

import org.apache.commons.io.FileUtils;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolveException;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.Nullable;
import quilt.internal.Constants;
import quilt.internal.tasks.DefaultMappingsTask;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;

public abstract class ExtractTinyMappingsTask extends DefaultMappingsTask {
    public static final String TINY_MAPPINGS_FILE_NAME_SUFFIX = "mappings.tiny";

    @Optional
    @InputFile
    public abstract RegularFileProperty getJarFile();

    @OutputFile
    public abstract RegularFileProperty getTinyFile();

    @Inject
    protected abstract ProviderFactory getProviders();

    @Inject
    protected abstract ObjectFactory getObjects();

    private final FileTree jarZipTree;

    public ExtractTinyMappingsTask() {
        super(Constants.Groups.SETUP);

        this.onlyIf(unused -> this.getJarFile().isPresent());

        // zipTree accesses the passed path lazily so passing jarFile here is ok
        this.jarZipTree = this.getProject().zipTree(this.getJarFile());
    }

    @TaskAction
    public void extractTinyMappings() throws IOException {
        FileUtils.copyFile(
            this.jarZipTree
                .getFiles()
                .stream()
                .filter(file -> file.getName().endsWith(TINY_MAPPINGS_FILE_NAME_SUFFIX))
                .findFirst()
                .orElseThrow(),
            this.getTinyFile().get().getAsFile()
        );
    }

    /**
     * Provides a single optional file from the passed {@link Configuration resolvableConfiguration}.
     *
     * @param resolvableConfiguration a {@link Configuration#isCanBeResolved() resolvable} {@link Configuration}
     *                               that should only hold an intermediary mappings jar
     *
     * @return a provider holding the contents of the passed {@link Configuration resolvableConfiguration} if
     * resolution succeeds and it contains exactly one file, or an empty provider if resolution fails
     *
     * @throws IllegalArgumentException if the passed {@link Configuration resolvableConfiguration} is not
     * {@link Configuration#isCanBeResolved() resolvable}
     * @throws IllegalStateException if the passed {@link Configuration resolvableConfiguration}
     * doesn't contain exactly one file
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
     * Provides a single required file from the passed {@link Configuration resolvableConfiguration}.
     *
     * @param resolvableConfiguration a {@link Configuration#isCanBeResolved() resolvable} {@link Configuration}
     *                               that should only hold an intermediary mappings jar
     *
     * @return a provider holding the contents of the passed {@link Configuration resolvableConfiguration}
     *
     * @throws IllegalArgumentException if the passed {@link Configuration resolvableConfiguration} is not
     * {@link Configuration#isCanBeResolved() resolvable}
     * @throws ResolveException if an error occurs in the resolution of the passed
     * {@link Configuration resolvableConfiguration}
     * @throws IllegalStateException if the passed {@link Configuration resolvableConfiguration}
     * doesn't contain exactly one file
     */
    public Provider<RegularFile> provideRequiredFile(Configuration resolvableConfiguration) {
        return this.provideFile(resolvableConfiguration, Configuration::getSingleFile);
    }

    protected Provider<RegularFile> provideFile(
        Configuration resolvableConfiguration, Function<Configuration, @Nullable File> resolver
    ) {
        return this.getProviders().provider(() -> {
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
