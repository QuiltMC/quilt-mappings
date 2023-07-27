package quilt.internal.decompile.vineflower;

import net.fabricmc.fernflower.api.IFabricJavadocProvider;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMethod;
import quilt.internal.decompile.javadoc.ClassJavadocProvider;
import quilt.internal.decompile.javadoc.FieldJavadocProvider;
import quilt.internal.decompile.javadoc.MethodJavadocProvider;

public class VineflowerJavadocProvider implements IFabricJavadocProvider {
    private final ClassJavadocProvider classJavadocProvider;
    private final FieldJavadocProvider fieldJavadocProvider;
    private final MethodJavadocProvider methodJavadocProvider;

    public VineflowerJavadocProvider(ClassJavadocProvider classJavadocProvider, FieldJavadocProvider fieldJavadocProvider, MethodJavadocProvider methodJavadocProvider) {
        this.classJavadocProvider = classJavadocProvider != null ? classJavadocProvider : ClassJavadocProvider.EMPTY;
        this.fieldJavadocProvider = fieldJavadocProvider != null ? fieldJavadocProvider : FieldJavadocProvider.EMPTY;
        this.methodJavadocProvider = methodJavadocProvider != null ? methodJavadocProvider : MethodJavadocProvider.EMPTY;
    }

    @Override
    public String getClassDoc(StructClass structClass) {
        boolean isRecord = (structClass.getAccessFlags() & 0x10000) != 0; // ACC_RECORD
        return classJavadocProvider.provideClassJavadoc(structClass.qualifiedName, isRecord);
    }

    @Override
    public String getFieldDoc(StructClass structClass, StructField structField) {
        return fieldJavadocProvider.provideFieldJavadoc(structField.getName(), structField.getDescriptor(), structClass.qualifiedName);
    }

    @Override
    public String getMethodDoc(StructClass structClass, StructMethod structMethod) {
        return methodJavadocProvider.provideMethodJavadoc(structMethod.getName(), structMethod.getDescriptor(), structClass.qualifiedName);
    }
}
