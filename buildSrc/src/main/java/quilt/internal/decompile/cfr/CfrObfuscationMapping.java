package quilt.internal.decompile.cfr;

import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Field;
import org.benf.cfr.reader.mapping.NullMapping;
import org.benf.cfr.reader.util.output.DelegatingDumper;
import org.benf.cfr.reader.util.output.Dumper;
import quilt.internal.decompile.javadoc.ClassJavadocProvider;
import quilt.internal.decompile.javadoc.FieldJavadocProvider;
import quilt.internal.decompile.javadoc.MethodJavadocProvider;

public class CfrObfuscationMapping extends NullMapping {
    private final ClassJavadocProvider classJavadocProvider;
    private final FieldJavadocProvider fieldJavadocProvider;
    private final MethodJavadocProvider methodJavadocProvider;

    public CfrObfuscationMapping(ClassJavadocProvider classJavadocProvider, FieldJavadocProvider fieldJavadocProvider, MethodJavadocProvider methodJavadocProvider) {
        this.classJavadocProvider = classJavadocProvider != null ? classJavadocProvider : ClassJavadocProvider.EMPTY;
        this.fieldJavadocProvider = fieldJavadocProvider != null ? fieldJavadocProvider : FieldJavadocProvider.EMPTY;
        this.methodJavadocProvider = methodJavadocProvider != null ? methodJavadocProvider : MethodJavadocProvider.EMPTY;
    }

    @Override
    public Dumper wrap(Dumper d) {
        return new JavadocDumper(d);
    }

    public class JavadocDumper extends DelegatingDumper {
        public JavadocDumper(Dumper delegate) {
            super(delegate);
        }

        @Override
        public Dumper dumpClassDoc(JavaTypeInstance owner) {
            String javadoc = classJavadocProvider.provide(owner.getRawName(), isRecord(owner));
            if (javadoc != null) {
                dumpJavadoc(javadoc);
            }

            return this;
        }

        @Override
        public Dumper dumpMethodDoc(MethodPrototype method) {
            String javadoc = methodJavadocProvider.provide(method.getName(), method.getOriginalDescriptor(), method.getOwner().getRawName());
            if (javadoc != null) {
                dumpJavadoc(javadoc);
            }

            return this;
        }

        @Override
        public Dumper dumpFieldDoc(Field field, JavaTypeInstance owner) {
            String javadoc = fieldJavadocProvider.provide(field.getFieldName(), field.getDescriptor(), owner.getRawName());
            if (javadoc != null) {
                dumpJavadoc(javadoc);
            }

            return this;
        }

        private boolean isRecord(JavaTypeInstance owner) {
            if (owner instanceof JavaRefTypeInstance) {
                ClassFile classFile = ((JavaRefTypeInstance) owner).getClassFile();
                return classFile.getClassSignature().getSuperClass().getRawName().equals("java.lang.Record");
            }

            return false;
        }

        private void dumpJavadoc(String javadoc) {
            if (javadoc == null || javadoc.isBlank()) {
                return;
            }

            print("/**").newln();

            for (String line : javadoc.split("\n")) {
                print(" * ").print(line).newln();
            }

            print(" */").newln();
        }
    }
}
