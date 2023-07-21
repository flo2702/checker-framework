package org.checkerframework.checker.initialization;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;

import org.checkerframework.checker.initialization.qual.FBCBottom;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.flow.CFAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.GenericAnnotatedTypeFactory;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

/**
 * The annotated type factory for the freedom-before-commitment type system. When using the
 * freedom-before-commitment type system as a subchecker, you must ensure that the parent checker
 * hooks into it properly. See {@link InitializationChecker} for further information.
 */
public class InitializationFieldAccessAnnotatedTypeFactory
        extends InitializationParentAnnotatedTypeFactory<CFValue, CFStore, CFTransfer, CFAnalysis> {

    public InitializationFieldAccessAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        postInit();
    }

    @Override
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        return Set.of(
                UnknownInitialization.class,
                UnderInitialization.class,
                Initialized.class,
                FBCBottom.class);
    }

    /**
     * This annotator should be added to {@link GenericAnnotatedTypeFactory#createTreeAnnotator} for
     * this Initialization Checker's subchecker. It ensures that the fields of an uninitialized
     * receiver have the top type in the parent checker's hierarchy.
     *
     * @see InitializationChecker#SUBCHECKER_CLASS
     */
    public static class CommitmentFieldAccessTreeAnnotator extends TreeAnnotator {

        /**
         * Creates a new CommitmentFieldAccessTreeAnnotator.
         *
         * @param atypeFactory the type factory belonging to the init checker's parent
         */
        public CommitmentFieldAccessTreeAnnotator(
                GenericAnnotatedTypeFactory<?, ?, ?, ?> atypeFactory) {
            super(atypeFactory);
        }

        @Override
        public Void visitIdentifier(IdentifierTree tree, AnnotatedTypeMirror p) {
            super.visitIdentifier(tree, p);

            // Only call computeFieldAccessType for actual field accesses, not for direct uses
            // of this and super.
            if (tree.getName().contentEquals("this") || tree.getName().contentEquals("super")) {
                return null;
            }

            computeFieldAccessType(tree, p);
            return null;
        }

        @Override
        public Void visitMemberSelect(MemberSelectTree tree, AnnotatedTypeMirror p) {
            super.visitMemberSelect(tree, p);
            computeFieldAccessType(tree, p);
            return null;
        }

        /**
         * Adapts the type of a field access depending on the field's declared type and the
         * receiver's initialization type.
         *
         * @param tree the field access
         * @param type the field access's unadapted type
         */
        private void computeFieldAccessType(ExpressionTree tree, AnnotatedTypeMirror type) {
            GenericAnnotatedTypeFactory<?, ?, ?, ?> factory =
                    (GenericAnnotatedTypeFactory<?, ?, ?, ?>) atypeFactory;
            InitializationFieldAccessAnnotatedTypeFactory initFactory =
                    factory.getChecker()
                            .getTypeFactoryOfSubchecker(InitializationFieldAccessChecker.class);
            Element element = TreeUtils.elementFromUse(tree);
            AnnotatedTypeMirror owner = initFactory.getReceiverType(tree);

            if (owner == null) {
                return;
            }

            if (type instanceof AnnotatedExecutableType) {
                return;
            }

            AnnotatedTypeMirror fieldAnnotations = factory.getAnnotatedType(element);

            // not necessary if there is an explicit UnknownInitialization
            // annotation on the field
            if (AnnotationUtils.containsSameByName(
                    fieldAnnotations.getAnnotations(), initFactory.UNKNOWN_INITIALIZATION)) {
                return;
            }
            if (!initFactory.isUnknownInitialization(owner)
                    && !initFactory.isUnderInitialization(owner)) {
                return;
            }

            TypeMirror fieldDeclarationType = element.getEnclosingElement().asType();
            boolean isOwnerInitialized =
                    initFactory.isInitializedForFrame(owner, fieldDeclarationType);

            if (!isOwnerInitialized && !factory.isComputingAnnotatedTypeMirrorOfLHS()) {
                // The receiver is not initialized for this frame and the type being computed is
                // not a LHS.
                // Replace all annotations with the top annotation for that hierarchy.
                type.clearAnnotations();
                type.addAnnotations(factory.getQualifierHierarchy().getTopAnnotations());
            }
        }
    }
}
