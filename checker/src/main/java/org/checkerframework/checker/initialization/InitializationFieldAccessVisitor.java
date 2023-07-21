package org.checkerframework.checker.initialization;

import com.sun.source.util.TreePath;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;

public class InitializationFieldAccessVisitor
        extends BaseTypeVisitor<InitializationFieldAccessAnnotatedTypeFactory> {

    public InitializationFieldAccessVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    public void visit(TreePath path) {
        // do nothing
    }
}
