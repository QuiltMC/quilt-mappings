package quilt.internal.mappingio;

import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link ForwardingMappingVisitor} that fills all destination namespaces with missing {@code <init>} names.
 */
public class CompleteInitializersVisitor extends ForwardingMappingVisitor {
    private String[] dstNames;
    private boolean visitingInitializer;

    public CompleteInitializersVisitor(MappingVisitor next) {
        super(next);
    }

    @Override
    public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
        this.dstNames = new String[dstNamespaces.size()];

        super.visitNamespaces(srcNamespace, dstNamespaces);
    }

    @Override
    public boolean visitMethod(String srcName, String srcDesc) throws IOException {
        this.visitingInitializer = srcName.equals("<init>") && srcDesc.endsWith(")V");

        return super.visitMethod(srcName, srcDesc);
    }

    @Override
    public void visitDstName(MappedElementKind targetKind, int namespace, String name) throws IOException {
        dstNames[namespace] = name;

        super.visitDstName(targetKind, namespace, name);
    }

    @Override
    public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
        if (this.visitingInitializer) {
            for (int i = 0; i < dstNames.length; i++) {
                if (dstNames[i] == null) {
                    super.visitDstName(targetKind, i, "<init>");
                }
            }
        }

        this.visitingInitializer = false;
        Arrays.fill(dstNames, null);

        return super.visitElementContent(targetKind);
    }
}
