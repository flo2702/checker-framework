package org.checkerframework.checker.initialization;

import com.sun.source.tree.ClassTree;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.dataflow.analysis.AnalysisResult;
import org.checkerframework.framework.flow.CFAbstractValue;

/**
 * The abstract type factory for the {@link InitializationFieldAccessSubchecker}.
 *
 * @see InitializationFieldAccessAnnotatedTypeFactory
 */
public abstract class InitializationFieldAccessAbstractAnnotatedTypeFactory<
                Value extends CFAbstractValue<Value>,
                Store extends InitializationAbstractStore<Value, Store>,
                Transfer extends InitializationAbstractTransfer<Value, Store, Transfer>,
                Analysis extends InitializationAbstractAnalysis<Value, Store, Transfer>>
        extends InitializationParentAnnotatedTypeFactory<Value, Store, Transfer, Analysis> {

    /**
     * Create a new InitializationFieldAccessAbstractAnnotatedTypeFactory.
     *
     * @param checker the checker to which the new type factory belongs
     */
    public InitializationFieldAccessAbstractAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    protected void performFlowAnalysis(ClassTree classTree) {
        // Only perform the analysis if initialization checking is turned on.
        if (!assumeInitialized) {
            super.performFlowAnalysis(classTree);
        }
    }

    /**
     * Returns the flow analysis.
     *
     * @return the flow analysis
     * @see #getFlowResult()
     */
    /*package-private*/ Analysis getAnalysis() {
        return analysis;
    }

    /**
     * Returns the result of the flow analysis. Invariant:
     *
     * <pre>
     *  scannedClasses.get(c) == FINISHED for some class c &rArr; flowResult != null
     * </pre>
     *
     * Note that flowResult contains analysis results for Trees from multiple classes which are
     * produced by multiple calls to performFlowAnalysis.
     *
     * @return the result of the flow analysis
     * @see #getAnalysis()
     */
    /*package-private*/ AnalysisResult<Value, Store> getFlowResult() {
        return flowResult;
    }
}
