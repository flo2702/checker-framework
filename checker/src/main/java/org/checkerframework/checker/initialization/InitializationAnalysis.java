package org.checkerframework.checker.initialization;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.flow.CFValue;
import org.checkerframework.javacutil.AnnotationMirrorSet;

import javax.lang.model.type.TypeMirror;

/** The default analysis class used by the freedom-before-commitment type system. */
public class InitializationAnalysis
        extends InitializationAbstractAnalysis<
                CFValue, InitializationStore, InitializationTransfer> {

    /**
     * Creates a new {@code InitializationAnalysis}.
     *
     * @param checker the checker
     * @param factory the factory
     */
    protected InitializationAnalysis(
            BaseTypeChecker checker,
            InitializationParentAnnotatedTypeFactory<
                            CFValue,
                            InitializationStore,
                            InitializationTransfer,
                            InitializationAnalysis>
                    factory) {
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
            AnnotationMirrorSet annotations, TypeMirror underlyingType) {
        return defaultCreateAbstractValue(this, annotations, underlyingType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public InitializationParentAnnotatedTypeFactory<
                    CFValue, InitializationStore, InitializationTransfer, InitializationAnalysis>
            getTypeFactory() {
        return (InitializationParentAnnotatedTypeFactory<
                        CFValue,
                        InitializationStore,
                        InitializationTransfer,
                        InitializationAnalysis>)
                super.getTypeFactory();
    }
}
