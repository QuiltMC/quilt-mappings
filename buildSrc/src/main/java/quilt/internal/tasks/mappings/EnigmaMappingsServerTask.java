package quilt.internal.tasks.mappings;

import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import quilt.internal.Constants;
import quilt.internal.tasks.MappingsTask;

import java.util.ArrayList;
import java.util.List;

public class EnigmaMappingsServerTask extends JavaExec implements MappingsTask {
	@InputFile
	private final RegularFileProperty jarToMap;

	private final List<String> args;

	public EnigmaMappingsServerTask() {
		Project project = this.getProject();
		this.setGroup(Constants.Groups.MAPPINGS_GROUP);
		this.getMainClass().set("org.quiltmc.enigma.network.DedicatedEnigmaServer");
		this.classpath(project.getConfigurations().getByName("enigmaRuntime"));
		this.jvmArgs("-Xmx2048m");

		this.jarToMap = getObjectFactory().fileProperty();

		var baseArgs = List.of(
				"-jar", this.jarToMap.get().getAsFile().getAbsolutePath(),
				"-mappings", project.file("mappings").getAbsolutePath(),
				"-profile", "enigma_profile.json"
		);
		this.args = new ArrayList<>(baseArgs);

		if (project.hasProperty("port")) {
			this.args.add("-port");
			this.args.add(project.getProperties().get("port").toString());
		}
		if (project.hasProperty("password")) {
			this.args.add("-password");
			this.args.add(project.getProperties().get("password").toString());
		}
		if (project.hasProperty("log")) {
			this.args.add("-log");
			this.args.add(project.getProperties().get("log").toString());
		} else {
			this.args.add("-log");
			this.args.add("build/logs/server.log");
		}
		if (project.hasProperty("args")) {
			this.args.addAll(List.of(project.getProperties().get("args").toString().split(" ")));
		}
	}

	@Override
	public void exec() {
		args(this.args);
		super.exec();
	}

	public RegularFileProperty getJarToMap() {
		return this.jarToMap;
	}
}
