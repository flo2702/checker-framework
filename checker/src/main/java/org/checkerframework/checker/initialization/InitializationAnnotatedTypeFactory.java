package org.checkerframework.checker.initialization;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.ReturnNode;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.LiteralTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.PropagationTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.javacutil.Pair;
import org.checkerframework.javacutil.TreeUtils;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;

/**
 * The annotated type factory for the freedom-before-commitment type system. When using the
 * freedom-before-commitment type system as a subchecker, you must ensure that the parent checker
 * hooks into it properly. See {@link InitializationChecker} for further information.
 */
public class InitializationAnnotatedTypeFactory extends InitializationParentAnnotatedTypeFactory {

    /**
     * Create a new InitializationAnnotatedTypeFactory.
     *
     * @param checker the checker to which the new type factory belongs
     */
    public InitializationAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        postInit();
    }

    @Override
    public InitializationChecker getChecker() {
        return (InitializationChecker) super.getChecker();
    }

    protected InitializationFieldAccessAnnotatedTypeFactory getFieldAccessFactory() {
        InitializationChecker checker = getChecker();
        BaseTypeChecker targetChecker = checker.getSubchecker(checker.getTargetCheckerClass());
        return targetChecker.getTypeFactoryOfSubchecker(InitializationFieldAccessChecker.class);
    }

    // Don't perform the same flow analysis twice.
    // Instead, reuse results from InitializationFieldAccessChecker

    @Override
    protected InitializationAnalysis createFlowAnalysis() {
        return getFieldAccessFactory().getAnalysis();
    }

    @Override
    protected void performFlowAnalysis(ClassTree classTree) {
        flowResult = getFieldAccessFactory().getFlowResult();
    }

    @Override
    public @Nullable InitializationStore getRegularExitStore(Tree tree) {
        return getFieldAccessFactory().getRegularExitStore(tree);
    }

    @Override
    public @Nullable InitializationStore getExceptionalExitStore(Tree tree) {
        return getFieldAccessFactory().getExceptionalExitStore(tree);
    }

    @Override
    public List<Pair<ReturnNode, TransferResult<CFValue, InitializationStore>>>
            getReturnStatementStores(MethodTree methodTree) {
        return getFieldAccessFactory().getReturnStatementStores(methodTree);
    }

    @Override
    protected TypeAnnotator createTypeAnnotator() {
        return new ListTypeAnnotator(
                super.createTypeAnnotator(), new CommitmentTypeAnnotator(this));
    }

    /**
     * Returns {@code false}. Redundancy in only the initialization hierarchy is ok and may even be
     * caused by implicit default annotations. The parent checker should determine whether to warn
     * about redundancy.
     */
    @Override
    public boolean shouldWarnIfStubRedundantWithBytecode() {
        return false;
    }

    @Override
    protected TreeAnnotator createTreeAnnotator() {
        // Don't call super.createTreeAnnotator because we want our CommitmentTreeAnnotator
        // instead of the default PropagationTreeAnnotator
        List<TreeAnnotator> treeAnnotators = new ArrayList<>(2);
        treeAnnotators.add(new LiteralTreeAnnotator(this).addStandardLiteralQualifiers());
        if (dependentTypesHelper.hasDependentAnnotations()) {
            treeAnnotators.add(dependentTypesHelper.createDependentTypesTreeAnnotator());
        }
        treeAnnotators.add(new CommitmentTreeAnnotator(this));
        return new ListTreeAnnotator(treeAnnotators);
    }

    /**
     * This type annotator adds the correct UnderInitialization annotation to super constructors.
     */
    protected class CommitmentTypeAnnotator extends TypeAnnotator {

        /**
         * Creates a new CommitmentTypeAnnotator.
         *
         * @param atypeFactory this factory
         */
        public CommitmentTypeAnnotator(InitializationAnnotatedTypeFactory atypeFactory) {
            super(atypeFactory);
        }

        @Override
        public Void visitExecutable(AnnotatedExecutableType t, Void p) {
            Void result = super.visitExecutable(t, p);
            Element elem = t.getElement();
            if (elem.getKind() == ElementKind.CONSTRUCTOR) {
                AnnotatedDeclaredType returnType = (AnnotatedDeclaredType) t.getReturnType();
                DeclaredType underlyingType = returnType.getUnderlyingType();
                returnType.replaceAnnotation(
                        getUnderInitializationAnnotationOfSuperType(underlyingType));
            }
            return result;
        }
    }

    /**
     * This tree annotator modifies the propagation tree annotator to add propagation rules for the
     * freedom-before-commitment system.
     */
    protected class CommitmentTreeAnnotator extends PropagationTreeAnnotator {

        /**
         * Creates a new CommitmentTreeAnnotator.
         *
         * @param atypeFactory this factory
         */
        public CommitmentTreeAnnotator(InitializationAnnotatedTypeFactory atypeFactory) {
            super(atypeFactory);
        }

        @Override
        public Void visitMethod(MethodTree tree, AnnotatedTypeMirror p) {
            Void result = super.visitMethod(tree, p);
            if (TreeUtils.isConstructor(tree)) {
                assert p instanceof AnnotatedExecutableType;
                AnnotatedExecutableType exeType = (AnnotatedExecutableType) p;
                DeclaredType underlyingType =
                        (DeclaredType) exeType.getReturnType().getUnderlyingType();
                AnnotationMirror a = getUnderInitializationAnnotationOfSuperType(underlyingType);
                exeType.getReturnType().replaceAnnotation(a);
            }
            return result;
        }

        @Override
        public Void visitNewClass(NewClassTree tree, AnnotatedTypeMirror p) {
            super.visitNewClass(tree, p);
            boolean allInitialized = true;
            Type type = ((JCTree) tree).type;
            for (ExpressionTree a : tree.getArguments()) {
                final AnnotatedTypeMirror t = getAnnotatedType(a);
                allInitialized &= (isInitialized(t) || isFbcBottom(t));
            }
            if (!allInitialized) {
                p.replaceAnnotation(createUnderInitializationAnnotation(type));
                return null;
            }
            p.replaceAnnotation(INITIALIZED);
            return null;
        }

        @Override
        public Void visitLiteral(LiteralTree tree, AnnotatedTypeMirror type) {
            if (tree.getKind() != Tree.Kind.NULL_LITERAL) {
                type.addAnnotation(INITIALIZED);
            }
            return super.visitLiteral(tree, type);
        }

        @Override
        public Void visitNewArray(NewArrayTree tree, AnnotatedTypeMirror type) {
            // The most precise element type for `new Object[] {null}` is @FBCBottom, but
            // the most useful element type is @Initialized (which is also accurate).
            AnnotatedArrayType arrayType = (AnnotatedArrayType) type;
            AnnotatedTypeMirror componentType = arrayType.getComponentType();
            if (componentType.hasEffectiveAnnotation(FBCBOTTOM)) {
                componentType.replaceAnnotation(INITIALIZED);
            }
            return null;
        }

        @Override
        public Void visitMemberSelect(
                MemberSelectTree tree, AnnotatedTypeMirror annotatedTypeMirror) {
            if (TreeUtils.isArrayLengthAccess(tree)) {
                annotatedTypeMirror.replaceAnnotation(INITIALIZED);
            }
            return super.visitMemberSelect(tree, annotatedTypeMirror);
        }

        /* The result of a binary or unary operator is either primitive or a String.
         * Primitives have no fields and are thus always @Initialized.
         * Since all String constructors return @Initialized strings, Strings
         * are also always @Initialized. */

        @Override
        public Void visitBinary(BinaryTree tree, AnnotatedTypeMirror type) {
            return null;
        }

        @Override
        public Void visitUnary(UnaryTree tree, AnnotatedTypeMirror type) {
            return null;
        }
    }
}
