package quilt.internal.decompile.vineflower;

import net.fabricmc.fernflower.api.IFabricJavadocProvider;
import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;
import org.jetbrains.java.decompiler.main.decompiler.BaseDecompiler;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import quilt.internal.decompile.AbstractDecompiler;
import quilt.internal.decompile.javadoc.ClassJavadocProvider;
import quilt.internal.decompile.javadoc.FieldJavadocProvider;
import quilt.internal.decompile.javadoc.MethodJavadocProvider;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

public class VineflowerDecompiler extends AbstractDecompiler {
    private IFabricJavadocProvider javadocProvider;
    private ClassJavadocProvider classJavadocProvider;
    private FieldJavadocProvider fieldJavadocProvider;
    private MethodJavadocProvider methodJavadocProvider;

    public VineflowerDecompiler(Project project) {
        super(project);
    }

    @Override
    public void decompile(File file, File outputDir, Map<String, Object> options, Collection<File> libraries) {
        Path outputPath = outputDir.toPath();

        // disable "inconsistent inner class" warning due to spam in the logs
        options.put(IFernflowerPreferences.WARN_INCONSISTENT_INNER_CLASSES, "0");

        IFabricJavadocProvider javadocProvider = null;
        if (this.javadocProvider != null) {
            javadocProvider = this.javadocProvider;
        } else if (hasMemberJavadocProvider()) {
            javadocProvider = new VineflowerJavadocProvider(this.classJavadocProvider, this.fieldJavadocProvider, this.methodJavadocProvider);
        }

        if (javadocProvider != null) {
            options.put(IFabricJavadocProvider.PROPERTY_NAME, javadocProvider);
        }

        IResultSaver resultSaver = new VineflowerResultSaver(outputPath);

        BaseDecompiler decompiler = new BaseDecompiler(resultSaver, options, new LoggerImpl());

        decompiler.addSource(file);
        for (File library : libraries) {
            decompiler.addLibrary(library);
        }

        decompiler.decompileContext();
    }

    private boolean hasMemberJavadocProvider() {
        return this.classJavadocProvider != null || this.fieldJavadocProvider != null || this.methodJavadocProvider != null;
    }

    public void withFabricJavadocProvider(IFabricJavadocProvider javadocProvider) {
        this.javadocProvider = javadocProvider;
    }

    @Override
    public void withClassJavadocProvider(ClassJavadocProvider javadocProvider) {
        this.classJavadocProvider = javadocProvider;
    }

    @Override
    public void withFieldJavadocProvider(FieldJavadocProvider javadocProvider) {
        this.fieldJavadocProvider = javadocProvider;
    }

    @Override
    public void withMethodJavadocProvider(MethodJavadocProvider javadocProvider) {
        this.methodJavadocProvider = javadocProvider;
    }

    private class LoggerImpl extends IFernflowerLogger {
        private static LogLevel getLogLevel(Severity severity) {
            return switch (severity) {
                case TRACE -> LogLevel.DEBUG;
                case INFO -> LogLevel.INFO;
                case WARN -> LogLevel.WARN;
                case ERROR -> LogLevel.ERROR;
            };
        }

        @Override
        public void writeMessage(String message, Severity severity) {
            getProject().getLogger().log(getLogLevel(severity), message);
        }

        @Override
        public void writeMessage(String message, Severity severity, Throwable t) {
            getProject().getLogger().log(getLogLevel(severity), message, t);
        }
    }
}
