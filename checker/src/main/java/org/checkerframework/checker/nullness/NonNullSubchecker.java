package org.checkerframework.checker.nullness;

import org.checkerframework.checker.initialization.InitializationFieldAccessChecker;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;

import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.processing.SupportedOptions;

@SupportedOptions({"assumeInitialized"})
public class NonNullSubchecker extends BaseTypeChecker {

    /** Default constructor for NonNullChecker. */
    public NonNullSubchecker() {}

    @Override
    public NonNullAnnotatedTypeFactory getTypeFactory() {
        return (NonNullAnnotatedTypeFactory) super.getTypeFactory();
    }

    @Override
    protected Set<Class<? extends BaseTypeChecker>> getImmediateSubcheckerClasses() {
        Set<Class<? extends BaseTypeChecker>> checkers = super.getImmediateSubcheckerClasses();
        if (!hasOptionNoSubcheckers("assumeInitialized")) {
            checkers.add(InitializationFieldAccessChecker.class);
        }
        return checkers;
    }

    @Override
    public NavigableSet<String> getSuppressWarningsPrefixes() {
        NavigableSet<String> result = super.getSuppressWarningsPrefixes();
        result.add("nullness");
        return result;
    }

    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new NonNullVisitor(this);
    }
}
