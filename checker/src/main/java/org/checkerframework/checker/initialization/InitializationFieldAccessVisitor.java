package org.checkerframework.checker.initialization;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.flow.CFValue;

/** The default visitor for the {@link InitializationFieldAccessSubchecker}. */
public class InitializationFieldAccessVisitor
        extends InitializationFieldAccessAbstractVisitor<
                CFValue,
                InitializationStore,
                InitializationTransfer,
                InitializationAnalysis,
                InitializationFieldAccessAnnotatedTypeFactory> {

    /**
     * Create an InitializationFieldAccessVisitor.
     *
     * @param checker the initialization field-access checker
     */
    public InitializationFieldAccessVisitor(BaseTypeChecker checker) {
        super(checker);
    }
}
