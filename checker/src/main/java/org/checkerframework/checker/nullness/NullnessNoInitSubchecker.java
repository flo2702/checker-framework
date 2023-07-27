package org.checkerframework.checker.nullness;

import org.checkerframework.checker.initialization.InitializationChecker;
import org.checkerframework.checker.initialization.InitializationDeclarationChecker;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;

import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.processing.SupportedOptions;

/**
 * The subchecker of the {@link NullnessChecker} which actually checks {@link NonNull} and related
 * qualifiers.
 *
 * <p>The {@link NullnessChecker} uses this checker as the target (see {@link
 * InitializationChecker#getTargetCheckerClass()}) for its initialization type system.
 */
@SupportedOptions({"assumeInitialized"})
public class NullnessNoInitSubchecker extends BaseTypeChecker {

    /** Default constructor for NonNullChecker. */
    public NullnessNoInitSubchecker() {}

    @Override
    public NullnessNoInitAnnotatedTypeFactory getTypeFactory() {
        return (NullnessNoInitAnnotatedTypeFactory) super.getTypeFactory();
    }

    @Override
    protected Set<Class<? extends BaseTypeChecker>> getImmediateSubcheckerClasses() {
        Set<Class<? extends BaseTypeChecker>> checkers = super.getImmediateSubcheckerClasses();
        if (!hasOption("assumeInitialized")) {
            checkers.add(InitializationDeclarationChecker.class);
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
        return new NullnessNoInitVisitor(this);
    }
}
