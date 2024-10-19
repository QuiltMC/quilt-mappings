package quilt.internal.tasks;

import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

/**
 * All tasks added by {@link quilt.internal.QuiltMappingsPlugin QuiltMappingsPlugin} share this type.
 */
public interface MappingsTask extends Task {
    @Inject
    ObjectFactory getObjects();
}
