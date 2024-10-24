package quilt.internal.plugin;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

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

    default Provider<Directory> getLibsDir() {
        return this.getBuildDir().dir("libs");
    }

    default Provider<Directory> getMinecraftDir() {
        return this.getBuildDir().dir("minecraft");
    }

    default Provider<Directory> getTempDir() {
        return this.getBuildDir().dir("temp");
    }

    default void provideDefaultError(Property<?> property, String errorMessage) {
        property.convention(this.getProviders().provider(() -> { throw new GradleException(errorMessage); }));
    }
}
