package org.checkerframework.checker.initialization;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFAbstractValue;

import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;

public class InitializationAnalysis
        extends CFAbstractAnalysis<
                InitializationValue, InitializationStore, InitializationTransfer> {

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
    public @Nullable InitializationValue createAbstractValue(
            Set<AnnotationMirror> annotations, TypeMirror underlyingType) {
        if (!CFAbstractValue.validateSet(annotations, underlyingType, qualifierHierarchy)) {
            return null;
        }
        return new InitializationValue(this, annotations, underlyingType);
    }
}
