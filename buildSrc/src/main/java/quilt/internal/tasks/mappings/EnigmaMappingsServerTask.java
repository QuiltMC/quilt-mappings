package quilt.internal.tasks.mappings;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.UntrackedTask;
import org.gradle.api.tasks.options.Option;
import org.gradle.work.DisableCachingByDefault;
import org.quiltmc.enigma.network.DedicatedEnigmaServer;

import java.util.ArrayList;
import java.util.List;

import static quilt.internal.util.ProviderUtil.toOptional;

/**
 * Starts a {@link DedicatedEnigmaServer}.
 * <p>
 * Optional inputs will be passed as command line args when starting the server if present.
 * <p>
 * Additional args can be specified when invoking the task using the
 * {@link JavaExec#setArgsString(String) --args} option.
 * <p>
 * If {@link quilt.internal.QuiltMappingsPlugin QuiltMappingsPlugin} is applied
 * the following gradle properties will be searched for default values:
 * <ul>
 *     <li> {@value quilt.internal.QuiltMappingsPlugin#ENIGMA_SERVER_PORT_PROP}
 *     for the {@link #getPort() port}
 *     <li> {@value quilt.internal.QuiltMappingsPlugin#ENIGMA_SERVER_PASSWORD_PROP}
 *     for the {@link #getPassword() password}
 *     <li> {@value quilt.internal.QuiltMappingsPlugin#ENIGMA_SERVER_LOG_PROP}
 *     for the {@link #getLog() log} path
 *     <li> {@value quilt.internal.QuiltMappingsPlugin#ENIGMA_SERVER_ARGS_PROP}
 *     for any additional command line args
 * </ul>
 */
public abstract class EnigmaMappingsServerTask extends AbstractEnigmaMappingsTask {
	public static final String PORT_OPTION = "port";
	public static final String PASSWORD_OPTION = "password";
	public static final String LOG_OPTION = "log";

	@Optional
	@Option(option = PORT_OPTION, description = "The port the Enigma server will run on.")
	@Input
	public abstract Property<String> getPort();

	@Optional
	@Option(option = PASSWORD_OPTION, description = "The password for the Enigma server.")
	@Input
	public abstract Property<String> getPassword();

	@Optional
	@Option(option = LOG_OPTION, description = "The path the Enigma server will write its log to.")
	@OutputFile
	public abstract RegularFileProperty getLog();

	public EnigmaMappingsServerTask() {
		// this configuration can stay here because it's what makes this an EnigmaMappingsServerTask
		this.getMainClass().set(DedicatedEnigmaServer.class.getName());
		this.getMainClass().finalizeValue();
	}

	@Override
	public void exec() {
		final List<String> optionalArgs = new ArrayList<>();

		toOptional(this.getPort()).ifPresent(port -> {
			optionalArgs.add("-" + PORT_OPTION);
			optionalArgs.add(port);
		});

		toOptional(this.getPassword()).ifPresent(password -> {
			optionalArgs.add("-" + PASSWORD_OPTION);
			optionalArgs.add(password);
		});

		toOptional(this.getLog().getAsFile()).ifPresent(log -> {
			optionalArgs.add("-" + LOG_OPTION);
			optionalArgs.add(log.getAbsolutePath());
		});

        this.args(optionalArgs);

		super.exec();
	}
}
