package quilt.internal.tasks.build;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.quiltmc.draftsman.asm.visitor.DraftsmanAdapterClassVisitor;

public abstract class EraseByteCodeTask extends TransformJarClassesTask {
    public static final String TASK_NAME = "eraseBytecode";

    public EraseByteCodeTask() {
        this.visitor(DraftsmanAdapterClassVisitor::new);
        // Set protected/package-private classes to public so that we don't have any access compile errors.
        // TODO: Fix this by putting the classes in the same package. Javadoc shows the modifier
        this.visitor(PublicClassVisitor::new);

        // filter out anonymous classes
        this.filter(classNode -> classNode.outerClass == null);
    }

    private static class PublicClassVisitor extends ClassVisitor {
        protected PublicClassVisitor(ClassVisitor classVisitor) {
            super(Opcodes.ASM9, classVisitor);
        }

        private static int toPublicAccess(int access) {
            if (
                (access & Opcodes.ACC_PROTECTED) != 0 || (
                    (access & Opcodes.ACC_PRIVATE) == 0
                    && (access & Opcodes.ACC_PUBLIC) == 0
                )
            ) {
                access = access & ~Opcodes.ACC_PROTECTED;
                access = access | Opcodes.ACC_PUBLIC;
            }

            return access;
        }

        @Override
        public void visit(
            int version, int access, String name, String signature, String superName, String[] interfaces
        ) {
            super.visit(version, toPublicAccess(access), name, signature, superName, interfaces);
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            super.visitInnerClass(name, outerName, innerName, toPublicAccess(access));
        }
    }
}
