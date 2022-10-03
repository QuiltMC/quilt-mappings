package quilt.internal.mappingio;

import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ForwardingMappingVisitor} that fills all destination namespaces with missing {@code <init>} names.
 */
public class CompleteInitializersVisitor extends ForwardingMappingVisitor {
    private int dstNamespaces;
    private boolean visitingInitializer;
    private final List<Integer> setNamespaces = new ArrayList<>();

    public CompleteInitializersVisitor(MappingVisitor next) {
        super(next);
    }

    @Override
    public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
        this.dstNamespaces = dstNamespaces.size();
        super.visitNamespaces(srcNamespace, dstNamespaces);
    }

    @Override
    public boolean visitMethod(String srcName, String srcDesc) throws IOException {
        this.visitingInitializer = srcName.equals("<init>") && srcDesc.endsWith(")V");
        this.setNamespaces.clear();

        return super.visitMethod(srcName, srcDesc);
    }

    @Override
    public void visitDstName(MappedElementKind targetKind, int namespace, String name) throws IOException {
        if (this.visitingInitializer) {
            this.setNamespaces.add(namespace);
        }

        super.visitDstName(targetKind, namespace, name);
    }

    @Override
    public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
        if (this.visitingInitializer) {
            for (int i = 0; i < this.dstNamespaces; i++) {
                if (!this.setNamespaces.contains(i)) {
                    this.visitDstName(targetKind, i, "<init>");
                }
            }
        }

        this.visitingInitializer = false;

        return super.visitElementContent(targetKind);
    }
}
