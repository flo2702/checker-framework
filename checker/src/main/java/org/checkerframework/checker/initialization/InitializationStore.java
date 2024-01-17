package org.checkerframework.checker.initialization;

import org.checkerframework.framework.flow.CFValue;

/** The default store used by the freedom-before-commitment type system. */
public class InitializationStore extends InitializationAbstractStore<CFValue, InitializationStore> {

    public InitializationStore(InitializationAnalysis analysis, boolean sequentialSemantics) {
        super(analysis, sequentialSemantics);
    }

    /**
     * A copy constructor.
     *
     * @param other the store to copy
     */
    public InitializationStore(InitializationStore other) {
        super(other);
    }
}
