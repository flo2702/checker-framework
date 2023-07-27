package org.checkerframework.checker.initialization;

import com.sun.source.util.TreePath;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;

/** The visitor for the {@link InitializationDeclarationChecker}. */
public class InitializationDeclarationVisitor
        extends BaseTypeVisitor<InitializationDeclarationAnnotatedTypeFactory> {

    public InitializationDeclarationVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    public void visit(TreePath path) {
        // do nothing
    }
}
