package org.checkerframework.checker.nullness;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFAbstractValue;
import org.checkerframework.javacutil.AnnotationMirrorSet;

import javax.lang.model.type.TypeMirror;

/**
 * The analysis class for the non-null type system (serves as factory for the transfer function,
 * stores and abstract values.
 */
public class NonNullAnalysis
        extends CFAbstractAnalysis<NonNullValue, NonNullStore, NonNullTransfer> {

    /**
     * Creates a new {@code NullnessAnalysis}.
     *
     * @param checker the checker
     * @param factory the factory
     */
    public NonNullAnalysis(BaseTypeChecker checker, NonNullAnnotatedTypeFactory factory) {
        super(checker, factory);
    }

    @Override
    public NonNullStore createEmptyStore(boolean sequentialSemantics) {
        return new NonNullStore(this, sequentialSemantics);
    }

    @Override
    public NonNullStore createCopiedStore(NonNullStore s) {
        return new NonNullStore(s);
    }

    @Override
    public NonNullValue createAbstractValue(
            AnnotationMirrorSet annotations, TypeMirror underlyingType) {
        if (!CFAbstractValue.validateSet(annotations, underlyingType, qualifierHierarchy)) {
            return null;
        }
        return new NonNullValue(this, annotations, underlyingType);
    }
}
