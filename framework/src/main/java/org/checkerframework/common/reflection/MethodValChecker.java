package org.checkerframework.common.reflection;

import java.util.LinkedHashSet;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.common.value.ValueChecker;
import org.checkerframework.framework.qual.StubFiles;

/**
 * The MethodVal Checker provides a sound estimate of the signature of Method objects.
 *
 * @checker_framework.manual #methodval-and-classval-checkers MethodVal Checker
 */
@StubFiles({"reflection.astub"})
public class MethodValChecker extends BaseTypeChecker {
    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new MethodValVisitor(this);
    }

    @Override
    protected LinkedHashSet<BaseTypeChecker> getImmediateSubcheckers() {
        // Don't call super otherwise MethodVal will be added as a subChecker
        // which creates a circular dependency.
        LinkedHashSet<BaseTypeChecker> subCheckers = new LinkedHashSet<>();
        subCheckers.add(new ValueChecker());
        subCheckers.add(new ClassValChecker());
        return subCheckers;
    }
}
