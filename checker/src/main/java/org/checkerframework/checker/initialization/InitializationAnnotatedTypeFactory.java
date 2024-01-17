package org.checkerframework.checker.initialization;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFValue;

/** The default annotated type factory used by the freedom-before-commitment type system. */
public class InitializationAnnotatedTypeFactory
        extends InitializationAbstractAnnotatedTypeFactory<
                CFValue, InitializationStore, InitializationTransfer, InitializationAnalysis> {

    /**
     * Create a new InitializationAbstractAnnotatedTypeFactory.
     *
     * @param checker the checker to which the new type factory belongs
     */
    public InitializationAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        postInit();
    }

    @Override
    public InitializationTransfer createFlowTransferFunction(
            CFAbstractAnalysis<CFValue, InitializationStore, InitializationTransfer> analysis) {
        return new InitializationTransfer((InitializationAnalysis) analysis);
    }

    @Override
    protected @Nullable InitializationFieldAccessAnnotatedTypeFactory getFieldAccessFactory() {
        InitializationChecker checker = getChecker();
        BaseTypeChecker targetChecker = checker.getSubchecker(checker.getTargetCheckerClass());
        return targetChecker.getTypeFactoryOfSubcheckerOrNull(
                InitializationFieldAccessSubchecker.class);
    }
}
