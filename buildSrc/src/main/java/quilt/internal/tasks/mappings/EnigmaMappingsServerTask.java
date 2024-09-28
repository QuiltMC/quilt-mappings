package quilt.internal.tasks.mappings;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;

import java.util.ArrayList;
import java.util.List;

public abstract class EnigmaMappingsServerTask extends JavaExec implements MappingsTask {
	@InputFile
	private final RegularFileProperty jarToMap;
	@InputDirectory
	protected abstract DirectoryProperty getMappings();

	private final List<String> serverArgs;

	public EnigmaMappingsServerTask() {
		final Project project = this.getProject();
		this.setGroup(Constants.Groups.MAPPINGS_GROUP);
		this.getMainClass().set("org.quiltmc.enigma.network.DedicatedEnigmaServer");
		this.classpath(project.getConfigurations().getByName("enigmaRuntime"));
		this.jvmArgs("-Xmx2048m");

		this.jarToMap = getObjectFactory().fileProperty();
		this.mappings = getObjectFactory().fileProperty();
		this.getMappings().convention(project.getLayout().dir(project.provider(() -> project.file("mappings"))));

		this.serverArgs = new ArrayList<>();

		if (project.hasProperty("port")) {
			this.serverArgs.add("-port");
			this.serverArgs.add(project.getProperties().get("port").toString());
		}
		if (project.hasProperty("password")) {
			this.serverArgs.add("-password");
			this.serverArgs.add(project.getProperties().get("password").toString());
		}
		if (project.hasProperty("log")) {
			this.serverArgs.add("-log");
			this.serverArgs.add(project.getProperties().get("log").toString());
		} else {
			this.serverArgs.add("-log");
			this.serverArgs.add("build/logs/server.log");
		}
		if (project.hasProperty("args")) {
			this.serverArgs.addAll(List.of(project.getProperties().get("args").toString().split(" ")));
		}
	}

	@Override
	public void exec() {
		var args = new ArrayList<>(List.of(
				"-jar", this.jarToMap.get().getAsFile().getAbsolutePath(),
				"-mappings", this.getMappings().get().getAsFile().getAbsolutePath(),
				"-profile", "enigma_profile.json"
		));
		args.addAll(this.serverArgs);

		args(args);
		super.exec();
	}

	public RegularFileProperty getJarToMap() {
		return this.jarToMap;
	}
}
