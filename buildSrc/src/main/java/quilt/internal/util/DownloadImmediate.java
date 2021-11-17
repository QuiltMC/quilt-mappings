package quilt.internal.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import de.undercouch.gradle.tasks.download.DownloadAction;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class DownloadImmediate {
    private DownloadImmediate(Project project, Task task, String src, File dest, boolean overwrite) throws IOException {
        DownloadAction downloadAction = new DownloadAction(project, task);
        downloadAction.src(new URL(src));
        downloadAction.dest(dest);
        downloadAction.overwrite(overwrite);

        downloadAction.execute();
    }

    public static class Builder {
        private final Project project;
        private final Task task;
        private String src;
        private File dest;
        private boolean overwrite;

        public Builder(Task task) {
            this.task = task;
            this.project = task.getProject();
        }

        public Builder src(String url) {
            this.src = url;
            return this;
        }

        public Builder dest(File file) {
            this.dest = file;
            return this;
        }

        public Builder overwrite(boolean overwrite) {
            this.overwrite = overwrite;
            return this;
        }

        public void download() throws IOException {
            new DownloadImmediate(project, task, src, dest, overwrite);
        }
    }
}
