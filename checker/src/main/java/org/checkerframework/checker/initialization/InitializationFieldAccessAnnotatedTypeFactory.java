package org.checkerframework.checker.initialization;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.dataflow.analysis.AnalysisResult;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.GenericAnnotatedTypeFactory;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

/** The type factory for the {@link InitializationFieldAccessSubchecker}. */
public class InitializationFieldAccessAnnotatedTypeFactory
        extends InitializationParentAnnotatedTypeFactory {

    /**
     * Create a new InitializationFieldAccessAnnotatedTypeFactory.
     *
     * @param checker the checker to which the new type factory belongs
     */
    public InitializationFieldAccessAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        postInit();
    }

    @Override
    protected InitializationAnalysis createFlowAnalysis() {
        return new InitializationAnalysis(checker, this);
    }

    @Override
    protected void performFlowAnalysis(ClassTree classTree) {
        // Only perform the analysis if initialization checking is turned on.
        if (!checker.hasOption("assumeInitialized")) {
            super.performFlowAnalysis(classTree);
            ;
        }
    }

    /**
     * Returns the flow analysis.
     *
     * @return the flow analysis
     * @see {@link #getFlowResult()}
     */
    InitializationAnalysis getAnalysis() {
        return analysis;
    }

    /**
     * Returns the result of the flow analysis. Invariant:
     *
     * <pre>
     *  scannedClasses.get(c) == FINISHED for some class c &rArr; flowResult != null
     * </pre>
     *
     * Note that flowResult contains analysis results for Trees from multiple classes which are
     * produced by multiple calls to performFlowAnalysis.
     *
     * @return the result of the flow analysis
     * @see #getAnalysis()
     */
    AnalysisResult<CFValue, InitializationStore> getFlowResult() {
        return flowResult;
    }

    /**
     * This annotator should be added to {@link GenericAnnotatedTypeFactory#createTreeAnnotator} for
     * the target checker. It ensures that the fields of an uninitialized receiver have the top type
     * in the parent checker's hierarchy.
     *
     * @see InitializationChecker#getTargetCheckerClass()
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
                            .getTypeFactoryOfSubchecker(InitializationFieldAccessSubchecker.class);
            Element element = TreeUtils.elementFromUse(tree);
            AnnotatedTypeMirror owner = initFactory.getReceiverType(tree);

            if (factory.getChecker().hasOption("assumeInitialized")) {
                return;
            }

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

            // If the field has been initialized, don't clear annotations.
            // This is ok even if the field was initialized with a non-invariant
            // value because in that case, there must have been an error before.
            // E.g.:
            //     { f1 = f2;
            //       f2 = f1; }
            // Here, we will get an error for the first assignment, but we won't get another
            // error for the second assignment.
            // See the AssignmentDuringInitialization test case.
            Tree declaration = initFactory.declarationFromElement(TreeUtils.elementFromTree(tree));
            InitializationStore store = initFactory.getStoreBefore(tree);
            boolean isFieldInitialized =
                    store != null
                            && TreeUtils.isSelfAccess(tree)
                            && initFactory
                                    .getInitializedFields(store, initFactory.getPath(tree))
                                    .contains(declaration);
            if (!isOwnerInitialized
                    && !isFieldInitialized
                    && !factory.isComputingAnnotatedTypeMirrorOfLHS()) {
                // The receiver is not initialized for this frame and the type being computed is
                // not a LHS.
                // Replace all annotations with the top annotation for that hierarchy.
                type.clearAnnotations();
                type.addAnnotations(factory.getQualifierHierarchy().getTopAnnotations());
            }
        }
    }
}
