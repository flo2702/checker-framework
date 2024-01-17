package org.checkerframework.checker.initialization;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.GenericAnnotatedTypeFactory;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.BugInCF;
import org.checkerframework.javacutil.TreeUtils;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

/**
 * Part of the freedom-before-commitment type system.
 *
 * <p>This annotator should be added to {@link GenericAnnotatedTypeFactory#createTreeAnnotator} for
 * the target checker. It ensures that the fields of an uninitialized receiver have the top type in
 * the parent checker's hierarchy.
 *
 * @see InitializationChecker#getTargetCheckerClass()
 */
public class InitializationFieldAccessTreeAnnotator extends TreeAnnotator {

    /** The value of the assumeInitialized option. */
    protected final boolean assumeInitialized;

    /**
     * The {@link InitializationFieldAccessAbstractAnnotatedTypeFactory} used by the target
     * checker's subchecker.
     */
    protected InitializationFieldAccessAbstractAnnotatedTypeFactory<?, ?, ?, ?> fieldAccessFactory =
            null;

    /**
     * Creates a new CommitmentFieldAccessTreeAnnotator.
     *
     * @param atypeFactory the type factory belonging to the init checker's parent
     */
    public InitializationFieldAccessTreeAnnotator(
            GenericAnnotatedTypeFactory<?, ?, ?, ?> atypeFactory) {
        super(atypeFactory);
        assumeInitialized = atypeFactory.getChecker().hasOption("assumeInitialized");
        if (!assumeInitialized) {
            for (BaseTypeChecker subchecker :
                    atypeFactory.getChecker().getUltimateParentChecker().getSubcheckers()) {
                if (subchecker instanceof InitializationFieldAccessSubchecker) {
                    fieldAccessFactory =
                            (InitializationFieldAccessAbstractAnnotatedTypeFactory<?, ?, ?, ?>)
                                    subchecker.getTypeFactory();
                }
            }
            if (fieldAccessFactory == null) {
                throw new BugInCF("Did not find InitializationFieldAccessSubchecker!");
            }
        }
    }

    @Override
    public Void visitIdentifier(IdentifierTree tree, AnnotatedTypeMirror p) {
        super.visitIdentifier(tree, p);
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
     * Adapts the type in the target checker hierarchy of a field access depending on the field's
     * declared type and the receiver's initialization type.
     *
     * @param tree the field access
     * @param type the field access's unadapted type
     */
    private void computeFieldAccessType(ExpressionTree tree, AnnotatedTypeMirror type) {
        GenericAnnotatedTypeFactory<?, ?, ?, ?> factory =
                (GenericAnnotatedTypeFactory<?, ?, ?, ?>) atypeFactory;

        // Don't adapt anything if initialization checking is turned off.
        if (assumeInitialized) {
            return;
        }

        // Don't adapt anything if "tree" is not actually a field access.

        // Don't adapt uses of the identifiers "this" or "super" that are not field accesses
        // (e.g., constructor calls or uses of an outer this).
        if (tree instanceof IdentifierTree) {
            IdentifierTree identTree = (IdentifierTree) tree;
            if (identTree.getName().contentEquals("this")
                    || identTree.getName().contentEquals("super")) {
                return;
            }
        }

        // Don't adapt method accesses.
        if (type instanceof AnnotatedTypeMirror.AnnotatedExecutableType) {
            return;
        }

        // Don't adapt trees that do not have a (explicit or implicit) receiver (e.g., local
        // variables).
        AnnotatedTypeMirror receiver = fieldAccessFactory.getReceiverType(tree);
        if (receiver == null) {
            return;
        }

        // Don't adapt trees whose receiver is initialized.
        if (!fieldAccessFactory.isUnknownInitialization(receiver)
                && !fieldAccessFactory.isUnderInitialization(receiver)) {
            return;
        }

        // Don't adapt trees with an explicit UnknownInitialization annotation on the field
        Element element = TreeUtils.elementFromUse(tree);
        AnnotatedTypeMirror fieldAnnotations = factory.getAnnotatedType(element);
        if (AnnotationUtils.containsSameByName(
                fieldAnnotations.getAnnotations(), fieldAccessFactory.UNKNOWN_INITIALIZATION)) {
            return;
        }

        TypeMirror fieldOwnerType = element.getEnclosingElement().asType();
        boolean isReceiverInitToOwner =
                fieldAccessFactory.isInitializedForFrame(receiver, fieldOwnerType);

        // If the field has been initialized, don't clear annotations.
        // This is ok even if the field was initialized with a non-invariant
        // value because in that case, there must have been an error before.
        // E.g.:
        //     { f1 = f2;
        //       f2 = f1; }
        // Here, we will get an error for the first assignment, but we won't get another
        // error for the second assignment.
        // See the AssignmentDuringInitialization test case.
        Tree fieldDeclarationTree = fieldAccessFactory.declarationFromElement(element);
        List<VariableTree> initFields =
                fieldAccessFactory.getInitializedFieldsBefore(
                        tree, fieldAccessFactory.getPath(tree));
        // If the field declaration is null (because the field is declared in bytecode),
        // or the store is null (because flow-sensitive refinement is turned off),
        // the field is considered uninitialized.
        // Fields of objects other than this are not tracked and thus also considered uninitialized.
        // Otherwise, check if the field is initialized in the given store.
        boolean isFieldInitialized =
                fieldDeclarationTree != null
                        && TreeUtils.isSelfAccess(tree)
                        && initFields != null
                        && initFields.contains(fieldDeclarationTree);
        if (!isReceiverInitToOwner
                && !isFieldInitialized
                && !factory.isComputingAnnotatedTypeMirrorOfLhs()) {
            // The receiver is not initialized for this frame and the type being computed is
            // not a LHS.
            // Replace all annotations with the top annotation for that hierarchy.
            type.clearAnnotations();
            type.addAnnotations(factory.getQualifierHierarchy().getTopAnnotations());
        }
    }
}
