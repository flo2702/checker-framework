package org.checkerframework.checker.initialization;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFValue;

import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;

/**
 * The analysis class for the initialization type system (serves as factory for the transfer
 * function, stores and abstract values.
 */
public class InitializationAnalysis
        extends CFAbstractAnalysis<CFValue, InitializationStore, InitializationTransfer> {

    protected InitializationAnalysis(
            BaseTypeChecker checker, InitializationAnnotatedTypeFactory factory) {
        super(checker, factory);
    }

    @Override
    public InitializationStore createEmptyStore(boolean sequentialSemantics) {
        return new InitializationStore(this, sequentialSemantics);
    }

    @Override
    public InitializationStore createCopiedStore(InitializationStore s) {
        return new InitializationStore(s);
    }

    @Override
    public @Nullable CFValue createAbstractValue(
            Set<AnnotationMirror> annotations, TypeMirror underlyingType) {
        return defaultCreateAbstractValue(this, annotations, underlyingType);
    }
}
