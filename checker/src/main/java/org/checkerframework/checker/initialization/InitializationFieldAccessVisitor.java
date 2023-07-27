package org.checkerframework.checker.initialization;

import com.sun.source.tree.ClassTree;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;

/** The visitor for the {@link InitializationFieldAccessChecker}. */
public class InitializationFieldAccessVisitor
        extends BaseTypeVisitor<InitializationFieldAccessAnnotatedTypeFactory> {

    public InitializationFieldAccessVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    public void processClassTree(ClassTree classTree) {
        // As stated in the documentation for the InitializationFieldAccessChecker
        // and InitializationChecker, this checker performs the flow analysis
        // (which is handled in the BaseTypeVisitor), but does not perform
        // any type checking.
        // Thus, this method does nothing.
    }
}
