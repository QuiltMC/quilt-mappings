package quilt.internal.decompile.cfr;

import org.benf.cfr.reader.Driver;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.AnalysisType;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.output.SinkDumperFactory;
import org.gradle.api.Project;
import quilt.internal.decompile.AbstractDecompiler;
import quilt.internal.decompile.javadoc.ClassJavadocProvider;
import quilt.internal.decompile.javadoc.FieldJavadocProvider;
import quilt.internal.decompile.javadoc.MethodJavadocProvider;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class CfrDecompiler extends AbstractDecompiler {
    private ClassJavadocProvider classJavadocProvider;
    private FieldJavadocProvider fieldJavadocProvider;
    private MethodJavadocProvider methodJavadocProvider;

    public CfrDecompiler(Project project) {
        super(project);
    }

    @Override
    public void decompile(File file, File outputDir, Map<String, Object> optionsMap, Collection<File> libraries) {
        Map<String, String> stringOptions = optionsMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
        Options options = OptionsImpl.getFactory().create(stringOptions);

        ClassFileSourceImpl classFileSource = new ClassFileSourceImpl(options);

        for (File library : libraries) {
            classFileSource.addJarContent(library.getAbsolutePath(), AnalysisType.JAR);
        }

        classFileSource.informAnalysisRelativePathDetail(null, null);

        DCCommonState state = new DCCommonState(options, classFileSource);

        if (hasMemberJavadocProvider()) {
            CfrObfuscationMapping obfuscationMapping = new CfrObfuscationMapping(this.classJavadocProvider, this.fieldJavadocProvider, this.methodJavadocProvider);
            state = new DCCommonState(state, obfuscationMapping);
        }

        CfrSinkFactory sinkFactory = new CfrSinkFactory(outputDir, getProject().getLogger());
        SinkDumperFactory sinkDumperFactory = new SinkDumperFactory(sinkFactory, options);

        Driver.doJar(state, file.getAbsolutePath(), AnalysisType.JAR, sinkDumperFactory);
    }

    private boolean hasMemberJavadocProvider() {
        return this.classJavadocProvider != null || this.fieldJavadocProvider != null || this.methodJavadocProvider != null;
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
}
