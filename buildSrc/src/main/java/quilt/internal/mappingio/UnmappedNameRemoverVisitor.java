package quilt.internal.mappingio;

import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

public class UnmappedNameRemoverVisitor extends ForwardingMappingVisitor {
    private final String targetNamespace;
    private final String unmappedNamespace;
    private final BiPredicate<MappedElementKind, String> unmappedPredicate;

    private boolean superVisitHeader;
    private String[] dstNames;
    private int targetNs;
    private int unmappedNs;

    private String srcName;

    public UnmappedNameRemoverVisitor(MappingVisitor next, String targetNamespace, String unmappedNamespace) {
        this(next, targetNamespace, unmappedNamespace, (kind, name) -> switch (kind) {
            case CLASS -> name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('$'))).startsWith("C_");
            case FIELD -> name.startsWith("f_");
            case METHOD -> name.startsWith("m_");
            default -> name == null;
        });
    }

    public UnmappedNameRemoverVisitor(MappingVisitor next, String targetNamespace, String unmappedNamespace, BiPredicate<MappedElementKind, String> unmappedPredicate) {
        super(next);
        this.targetNamespace = targetNamespace;
        this.unmappedNamespace = unmappedNamespace;
        this.unmappedPredicate = unmappedPredicate;
    }

    @Override
    public boolean visitHeader() throws IOException {
        this.superVisitHeader = super.visitHeader();

        return true;
    }

    private int namespaceId(String namespace, String srcNamespace, List<String> dstNamespaces) {
        if (srcNamespace.equals(namespace)) {
            return -1;
        } else {
            int i = dstNamespaces.indexOf(namespace);
            if (i == -1) {
                throw new IllegalArgumentException("Invalid namespace " + namespace + ": is not " + srcNamespace + " or in " + dstNamespaces);
            }

            return i;
        }
    }

    @Override
    public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
        this.dstNames = new String[dstNamespaces.size()];

        this.targetNs = dstNamespaces.indexOf(this.targetNamespace);
        this.unmappedNs = namespaceId(this.unmappedNamespace, srcNamespace, dstNamespaces);

        if (this.targetNs == -1) {
            throw new IllegalStateException("Invalid namespace " + this.targetNamespace + ": is not in the destination namespaces " + dstNamespaces);
        }

        if (this.superVisitHeader) {
            super.visitNamespaces(srcNamespace, dstNamespaces);
        }
    }

    @Override
    public void visitMetadata(String key, String value) throws IOException {
        if (this.superVisitHeader) {
            super.visitMetadata(key, value);
        }
    }

    @Override
    public boolean visitContent() throws IOException {
        return super.visitContent();
    }

    @Override
    public boolean visitClass(String srcName) throws IOException {
        this.srcName = srcName;

        return super.visitClass(srcName);
    }

    @Override
    public boolean visitField(String srcName, String srcDesc) throws IOException {
        this.srcName = srcName;

        return super.visitField(srcName, srcDesc);
    }

    @Override
    public boolean visitMethod(String srcName, String srcDesc) throws IOException {
        this.srcName = srcName;

        return super.visitMethod(srcName, srcDesc);
    }

    @Override
    public boolean visitMethodArg(int argPosition, int lvIndex, String srcName) throws IOException {
        this.srcName = srcName;

        return super.visitMethodArg(argPosition, lvIndex, srcName);
    }

    @Override
    public boolean visitMethodVar(int lvtRowIndex, int lvIndex, int startOpIdx, String srcName) throws IOException {
        this.srcName = srcName;

        return super.visitMethodVar(lvtRowIndex, lvIndex, startOpIdx, srcName);
    }

    @Override
    public void visitDstName(MappedElementKind targetKind, int namespace, String name) throws IOException {
        this.dstNames[namespace] = name;

        if (targetKind == MappedElementKind.CLASS || namespace != this.targetNs) {
            super.visitDstName(targetKind, namespace, name);
        }
    }

    @Override
    public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
        if (targetKind != MappedElementKind.CLASS) {
            String name = this.dstNames[this.targetNs];
            String unmappedName = this.unmappedNs == -1 ? this.srcName : this.dstNames[this.unmappedNs];

            if (!Objects.equals(unmappedName, name) || !this.unmappedPredicate.test(targetKind, name)) {
                super.visitDstName(targetKind, this.targetNs, name);
            }
        }
        // TODO: Handle classes

        Arrays.fill(this.dstNames, null);

        return super.visitElementContent(targetKind);
    }
}
