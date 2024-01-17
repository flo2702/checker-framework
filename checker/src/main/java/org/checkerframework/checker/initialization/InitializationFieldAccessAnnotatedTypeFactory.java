package org.checkerframework.checker.initialization;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFValue;

/** The default type factory for the {@link InitializationFieldAccessSubchecker}. */
public class InitializationFieldAccessAnnotatedTypeFactory
        extends InitializationFieldAccessAbstractAnnotatedTypeFactory<
                CFValue, InitializationStore, InitializationTransfer, InitializationAnalysis> {

    /**
     * Create a new InitializationFieldAccessAbstractAnnotatedTypeFactory.
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
    public InitializationTransfer createFlowTransferFunction(
            CFAbstractAnalysis<CFValue, InitializationStore, InitializationTransfer> analysis) {
        return new InitializationTransfer((InitializationAnalysis) analysis);
    }
}
