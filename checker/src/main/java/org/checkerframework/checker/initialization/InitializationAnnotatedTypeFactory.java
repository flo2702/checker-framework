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
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;

import org.checkerframework.checker.initialization.qual.NotOnlyInitialized;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
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
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.TreePathUtil;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.TypesUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

/**
 * The annotated type factory for the freedom-before-commitment type system. When using the
 * freedom-before-commitment type system as a subchecker, you must ensure that the parent checker
 * hooks into it properly. See {@link InitializationChecker} for further information.
 */
public class InitializationAnnotatedTypeFactory
        extends InitializationParentAnnotatedTypeFactory<
                CFValue, InitializationStore, InitializationTransfer, InitializationAnalysis> {

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

    @Override
    public void postAsMemberOf(
            AnnotatedTypeMirror type, AnnotatedTypeMirror owner, Element element) {
        super.postAsMemberOf(type, owner, element);

        if (element.getKind().isField()) {
            Collection<? extends AnnotationMirror> declaredFieldAnnotations =
                    getDeclAnnotations(element);
            AnnotatedTypeMirror fieldAnnotations = getAnnotatedType(element);
            computeFieldAccessInitializationType(
                    type, declaredFieldAnnotations, owner, fieldAnnotations);
        }
    }

    /**
     * Determine the initialization type of a field access (implicit or explicit) based on the
     * receiver type and the declared annotations for the field.
     *
     * @param type type of the field access expression
     * @param declaredFieldAnnotations declared annotations on the field
     * @param receiverType inferred annotations of the receiver
     * @param fieldType inferred annotations of the field
     */
    private void computeFieldAccessInitializationType(
            AnnotatedTypeMirror type,
            Collection<? extends AnnotationMirror> declaredFieldAnnotations,
            AnnotatedTypeMirror receiverType,
            AnnotatedTypeMirror fieldType) {
        // Primitive values have no fields and are thus always @Initialized.
        if (TypesUtils.isPrimitive(type.getUnderlyingType())) {
            return;
        }
        // not necessary if there is an explicit UnknownInitialization
        // annotation on the field
        if (AnnotationUtils.containsSameByName(
                fieldType.getAnnotations(), UNKNOWN_INITIALIZATION)) {
            return;
        }
        if (isUnknownInitialization(receiverType) || isUnderInitialization(receiverType)) {
            if (AnnotationUtils.containsSame(declaredFieldAnnotations, NOT_ONLY_INITIALIZED)) {
                type.replaceAnnotation(UNKNOWN_INITIALIZATION);
            } else {
                type.replaceAnnotation(INITIALIZED);
            }

            if (!AnnotationUtils.containsSame(declaredFieldAnnotations, NOT_ONLY_INITIALIZED)) {
                // add root annotation for all other hierarchies, and
                // Initialized for the initialization hierarchy
                type.replaceAnnotation(INITIALIZED);
            }
        }
    }

    /**
     * Side-effects argument {@code selfType} to make it @Initialized or @UnderInitialization,
     * depending on whether all fields have been set.
     *
     * @param tree a tree
     * @param selfType the type to side-effect
     * @param path a path
     */
    @Override
    protected void setSelfTypeInInitializationCode(
            Tree tree, AnnotatedDeclaredType selfType, TreePath path) {
        ClassTree enclosingClass = TreePathUtil.enclosingClass(path);
        Type classType = ((JCTree) enclosingClass).type;
        AnnotationMirror annotation = null;

        // If all fields are initialized-only, and they are all initialized,
        // then:
        //  - if the class is final, this is @Initialized
        //  - otherwise, this is @UnderInitialization(CurrentClass) as
        //    there might still be subclasses that need initialization.
        if (areAllFieldsInitializedOnly(enclosingClass)) {
            InitializationStore store = getStoreBefore(tree);
            if (store != null
                    && getUninitializedFields(store, path, false, Collections.emptyList())
                            .isEmpty()) {
                if (classType.isFinal()) {
                    annotation = INITIALIZED;
                } else {
                    annotation = createUnderInitializationAnnotation(classType);
                }
            }
        }

        if (annotation == null) {
            annotation = getUnderInitializationAnnotationOfSuperType(classType);
        }
        selfType.replaceAnnotation(annotation);
    }

    /**
     * Are all fields initialized-only?
     *
     * @param classTree the class to query
     * @return true if all fields are initialized-only
     */
    protected boolean areAllFieldsInitializedOnly(ClassTree classTree) {
        for (Tree member : classTree.getMembers()) {
            if (member.getKind() != Tree.Kind.VARIABLE) {
                continue;
            }
            VariableTree var = (VariableTree) member;
            VariableElement varElt = TreeUtils.elementFromDeclaration(var);
            // var is not initialized-only
            if (getDeclAnnotation(varElt, NotOnlyInitialized.class) != null) {
                // var is not static -- need a check of initializer blocks,
                // not of constructor which is where this is used
                if (!varElt.getModifiers().contains(Modifier.STATIC)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the fields that are not yet initialized in a given store.
     *
     * @param store a store
     * @param path the current path, used to determine the current class
     * @param isStatic whether to report static fields or instance fields
     * @param receiverAnnotations the annotations on the receiver
     * @return the fields that are not yet initialized in a given store
     */
    public List<VariableTree> getUninitializedFields(
            InitializationStore store,
            TreePath path,
            boolean isStatic,
            Collection<? extends AnnotationMirror> receiverAnnotations) {
        ClassTree currentClass = TreePathUtil.enclosingClass(path);
        List<VariableTree> fields = InitializationChecker.getAllFields(currentClass);
        List<VariableTree> uninit = new ArrayList<>();
        for (VariableTree field : fields) {
            if (isUnused(field, receiverAnnotations)) {
                continue; // don't consider unused fields
            }
            VariableElement fieldElem = TreeUtils.elementFromDeclaration(field);
            if (ElementUtils.isStatic(fieldElem) == isStatic) {
                if (!store.isFieldInitialized(fieldElem)) {
                    uninit.add(field);
                }
            }
        }
        return uninit;
    }

    /**
     * Returns the fields that are initialized in the given store.
     *
     * @param store a store
     * @param path the current path; used to compute the current class
     * @return the fields that are initialized in the given store
     */
    public List<VariableTree> getInitializedFields(InitializationStore store, TreePath path) {
        // TODO: Instead of passing the TreePath around, can we use
        // getCurrentClassTree?
        ClassTree currentClass = TreePathUtil.enclosingClass(path);
        List<VariableTree> fields = InitializationChecker.getAllFields(currentClass);
        List<VariableTree> initializedFields = new ArrayList<>();
        for (VariableTree field : fields) {
            VariableElement fieldElem = TreeUtils.elementFromDeclaration(field);
            if (!ElementUtils.isStatic(fieldElem)) {
                if (store.isFieldInitialized(fieldElem)) {
                    initializedFields.add(field);
                }
            }
        }
        return initializedFields;
    }

    @Override
    public boolean isNotFullyInitializedReceiver(MethodTree methodTree) {
        if (super.isNotFullyInitializedReceiver(methodTree)) {
            return true;
        }
        final AnnotatedDeclaredType receiverType =
                analysis.getTypeFactory().getAnnotatedType(methodTree).getReceiverType();
        if (receiverType != null) {
            return isUnknownInitialization(receiverType) || isUnderInitialization(receiverType);
        } else {
            // There is no receiver e.g. in static methods.
            return false;
        }
    }

    @Override
    protected InitializationAnalysis createFlowAnalysis() {
        return new InitializationAnalysis(checker, this);
    }

    @Override
    public InitializationTransfer createFlowTransferFunction(
            CFAbstractAnalysis<CFValue, InitializationStore, InitializationTransfer> analysis) {
        return new InitializationTransfer((InitializationAnalysis) analysis);
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
