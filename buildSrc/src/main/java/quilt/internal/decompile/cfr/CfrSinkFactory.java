package quilt.internal.decompile.cfr;

import org.benf.cfr.reader.api.OutputSinkFactory;
import org.benf.cfr.reader.api.SinkReturns;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class CfrSinkFactory implements OutputSinkFactory {
    private final Logger logger;
    private final File outputDir;

    public CfrSinkFactory(File outputDir, Logger logger) {
        this.outputDir = outputDir;
        this.logger = logger;
    }

    private static void writeDecompiled(SinkReturns.Decompiled decompiled, File outputDir) {
        String packageName = decompiled.getPackageName().replace(".", "/");
        if (!packageName.isEmpty()) {
            packageName += "/";
        }

        String fileName = decompiled.getClassName() + ".java";
        String path = packageName + fileName;

        try {
            Path outputFile = outputDir.toPath().resolve(path);
            if (!Files.exists(outputFile.getParent())) {
                Files.createDirectories(outputFile.getParent());
            }

            Files.writeString(outputFile, decompiled.getJava());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write decompiled file " + path, e);
        }
    }

    @Override
    public List<SinkClass> getSupportedSinks(SinkType sinkType, Collection<SinkClass> available) {
        return switch (sinkType) {
            case JAVA -> List.of(SinkClass.DECOMPILED);
            case EXCEPTION -> List.of(SinkClass.STRING, SinkClass.EXCEPTION_MESSAGE);
            case PROGRESS -> List.of(SinkClass.STRING);
            default -> Collections.emptyList();
        };
    }

    @Override
    public <T> Sink<T> getSink(SinkType sinkType, SinkClass sinkClass) {
        return switch (sinkType) {
            case JAVA -> (d -> writeDecompiled((SinkReturns.Decompiled) d, outputDir));
            case EXCEPTION -> (e -> logger.error((String) e));
            case PROGRESS -> (p -> logger.debug((String) p));
            default -> null;
        };
    }
}
