package org.checkerframework.checker.initialization;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.ReturnNode;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.javacutil.Pair;

import java.util.List;

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
}
