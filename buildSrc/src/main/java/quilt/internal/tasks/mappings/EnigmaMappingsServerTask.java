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

	public EnigmaMappingsServerTask() {
		this.setGroup(Constants.Groups.MAPPINGS_GROUP);
		this.getMainClass().set("org.quiltmc.enigma.network.DedicatedEnigmaServer");
		this.classpath(getProject().getConfigurations().getByName("enigmaRuntime"));
		this.jvmArgs("-Xmx2048m");

		this.jarToMap = getObjectFactory().fileProperty();
	}

	@Override
	public void exec() {
		Project project = this.getProject();
		var baseArgs = List.of(
				"-jar", this.jarToMap.get().getAsFile().getAbsolutePath(),
				"-mappings", project.file("mappings").getAbsolutePath(),
				"-profile", "enigma_profile.json"
		);
		var args = new ArrayList<>(baseArgs);

		if (project.hasProperty("port")) {
			args.add("-port");
			args.add(project.getProperties().get("port").toString());
		}
		if (project.hasProperty("password")) {
			args.add("-password");
			args.add(project.getProperties().get("password").toString());
		}
		if (project.hasProperty("log")) {
			args.add("-log");
			args.add(project.getProperties().get("log").toString());
		} else {
			args.add("-log");
			args.add("build/logs/server.log");
		}
		if (project.hasProperty("args")) {
			args.addAll(List.of(project.getProperties().get("args").toString().split(" ")));
		}

		args(args);
		super.exec();
	}

	public RegularFileProperty getJarToMap() {
		return jarToMap;
	}
}
