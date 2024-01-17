package org.checkerframework.checker.initialization;

import org.checkerframework.framework.flow.CFValue;

/** The default transfer function used by the freedom-before-commitment type system. */
public class InitializationTransfer
        extends InitializationAbstractTransfer<
                CFValue, InitializationStore, InitializationTransfer> {

    /**
     * Create a new InitializationTransfer for the given analysis.
     *
     * @param analysis init analysis.
     */
    public InitializationTransfer(InitializationAnalysis analysis) {
        super(analysis);
    }
}
