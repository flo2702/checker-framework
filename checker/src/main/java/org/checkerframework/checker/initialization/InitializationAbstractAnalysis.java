package org.checkerframework.checker.initialization;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFAbstractValue;
import org.checkerframework.framework.type.GenericAnnotatedTypeFactory;

/**
 * An abstract analysis for the freedom-before-commitment type system (serves as factory for the
 * transfer function, stores, and abstract values.
 *
 * @see InitializationAnalysis
 */
public abstract class InitializationAbstractAnalysis<
                Value extends CFAbstractValue<Value>,
                Store extends InitializationAbstractStore<Value, Store>,
                Transfer extends InitializationAbstractTransfer<Value, Store, Transfer>>
        extends CFAbstractAnalysis<Value, Store, Transfer> {

    /**
     * Creates a new {@code InitializationAbstractAnalysis}.
     *
     * @param checker the checker
     * @param factory the factory
     */
    protected InitializationAbstractAnalysis(
            BaseTypeChecker checker,
            GenericAnnotatedTypeFactory<
                            Value,
                            Store,
                            Transfer,
                            ? extends CFAbstractAnalysis<Value, Store, Transfer>>
                    factory) {
        super(checker, factory);
    }
}
