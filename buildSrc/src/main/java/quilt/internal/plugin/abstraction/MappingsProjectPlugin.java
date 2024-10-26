package quilt.internal.plugin.abstraction;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolveException;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.jetbrains.annotations.Nullable;
import quilt.internal.Constants;

import javax.inject.Inject;
import java.io.File;
import java.util.function.Function;

/**
 * All Quilt Mappings plugins share this type.
 * <p>
 * Offers some common methods to all Quilt Mappings plugins:
 * <ul>
 *     <li> {@linkplain Inject injects} some of Gradle's services
 *     <li> adds methods to access some of Gradle's and Quilt's conventional directories
 *     <li> adds miscellaneous convenience methods used by multiple Quilt Mappings plugins
 * </ul>
 */
public interface MappingsProjectPlugin extends Plugin<Project> {
    @Inject
    ProviderFactory getProviders();

    @Inject
    ObjectFactory getObjects();

    @Inject
    ProjectLayout getLayout();

    default Directory getProjectDir() {
        return this.getLayout().getProjectDirectory();
    }

    // TODO is it important that this is in .gradle/ instead of build/?
    //  It means it doesn't get cleaned, and idk how to retrieve the configured gradle project cache dir
    default Directory getTargetsDir() {
        return this.getProjectDir().dir(".gradle/targets");
    }

    default DirectoryProperty getBuildDir() {
        return this.getLayout().getBuildDirectory();
    }

    default Provider<Directory> getMappingsDir() {
        return this.getBuildDir().dir("mappings");
    }

    default Provider<Directory> getMinecraftDir() {
        return this.getBuildDir().dir("minecraft");
    }

    default Provider<Directory> getTempDir() {
        return this.getBuildDir().dir("temp");
    }

    default Provider<RegularFile> provideMappingsDest(String mappingsName, String fileExt) {
        return this.getMinecraftDir().map(dir ->
            dir.file(Constants.MINECRAFT_VERSION + "-" + mappingsName + "." + fileExt)
        );
    };

    default void provideDefaultError(Property<?> property, String errorMessage) {
        property.convention(this.getProviders().provider(() -> { throw new GradleException(errorMessage); }));
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
    default Provider<RegularFile> provideOptionalFile(Configuration resolvableConfiguration) {
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
    default Provider<RegularFile> provideRequiredFile(Configuration resolvableConfiguration) {
        return this.provideFile(resolvableConfiguration, Configuration::getSingleFile);
    }

    default Provider<RegularFile> provideFile(
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
