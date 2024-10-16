package quilt.internal.util;

import de.undercouch.gradle.tasks.download.DownloadAction;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Downloader {
    private final Project project;
    private final Task task;
    private URL src;
    private File dest;
    private boolean overwrite;

    public Downloader(Task task) {
        this.task = task;
        this.project = task.getProject();
    }

    public Downloader src(String url) {
        try {
            this.src = new URI(url).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL", e);
        }

        return this;
    }

    public Downloader dest(File file) {
        this.dest = file;
        return this;
    }

    public Downloader overwrite(boolean overwrite) {
        this.overwrite = overwrite;
        return this;
    }

    public void download() throws IOException {
        // TODO I *think* this project access is ok.
        //  On Gradle 4.3+ it only accesses the project's layout.
        //  It uses the layout to create files, but so long as we only pass it task outputs it should be fine.
        //  But ideally we'd download things some other way.
        //  Some alternatives are listed here, but idk if any have the fancy progress bar:
        //  https://github.com/gradle/gradle/issues/28530
        final DownloadAction downloadAction = new DownloadAction(this.project, this.task);
        downloadAction.src(this.src);
        downloadAction.dest(this.dest);
        downloadAction.overwrite(this.overwrite);

        downloadAction.execute();
    }
}
