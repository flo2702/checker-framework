package org.checkerframework.checker.initialization;

import com.sun.source.tree.ClassTree;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.flow.CFAbstractValue;

/**
 * The abstract visitor for the {@link InitializationFieldAccessSubchecker}.
 *
 * @see InitializationFieldAccessVisitor
 */
public class InitializationFieldAccessAbstractVisitor<
                Value extends CFAbstractValue<Value>,
                Store extends InitializationAbstractStore<Value, Store>,
                Transfer extends InitializationAbstractTransfer<Value, Store, Transfer>,
                Analysis extends InitializationAbstractAnalysis<Value, Store, Transfer>,
                Factory extends
                        InitializationFieldAccessAbstractAnnotatedTypeFactory<
                                        Value, Store, Transfer, Analysis>>
        extends BaseTypeVisitor<Factory> {

    /** The value of the assumeInitialized option. */
    private final boolean assumeInitialized;

    /**
     * Create an InitializationFieldAccessVisitor.
     *
     * @param checker the initialization field-access checker
     */
    public InitializationFieldAccessAbstractVisitor(BaseTypeChecker checker) {
        super(checker);
        assumeInitialized = checker.hasOption("assumeInitialized");
    }

    @Override
    public void processClassTree(ClassTree classTree) {
        // As stated in the documentation for the InitializationFieldAccessChecker
        // and InitializationChecker, this checker performs the flow analysis
        // (which is handled in the BaseTypeVisitor), but does not perform
        // any type checking.
        // Thus, this method does nothing but scan through the members.
        if (!assumeInitialized) {
            scan(classTree.getMembers(), null);
        }
    }
}
