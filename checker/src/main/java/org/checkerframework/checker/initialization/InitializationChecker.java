package org.checkerframework.checker.initialization;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

import org.checkerframework.checker.initialization.InitializationFieldAccessAnnotatedTypeFactory.CommitmentFieldAccessTreeAnnotator;
import org.checkerframework.checker.nullness.NullnessChecker;
import org.checkerframework.checker.nullness.NullnessNoInitAnnotatedTypeFactory;
import org.checkerframework.checker.nullness.NullnessNoInitSubchecker;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.InvariantQualifier;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.processing.SupportedOptions;

/**
 * Tracks whether a value is initialized (all its fields are set), and checks that values are
 * initialized before being used. Implements the freedom-before-commitment scheme for
 * initialization, augmented by type frames.
 *
 * <p>Because there is a cyclic dependency between this type system and the target type system,
 * using this checker is more complex than for others. Specifically, the target checker must:
 *
 * <ol>
 *   <li>Use a subclass of this checker as its parent checker. This is necessary because this
 *       checker is dependent on the target checker to know which fields should be checked for
 *       initialization, and when such a field is initialized: A field is checked for initialization
 *       if its declared type has an {@link InvariantQualifier} (e.g., {@link NonNull}). Such a
 *       field becomes initialized when its refined type has that same invariant qualifier (which
 *       can happen either by assigning the field or by contract annotation like {@link
 *       EnsuresNonNull}). You can look at the {@link NullnessChecker} for an example: The
 *       NullnessChecker is a subclass of this checker and uses the {@link NullnessNoInitSubchecker}
 *       as the target checker.
 *   <li>Use the {@link InitializationFieldAccessChecker} as a subchecker and add its {@link
 *       CommitmentFieldAccessTreeAnnotator} as a tree annotator. This is necessary to give possibly
 *       uninitialized fields the top type of the target hierarchy (e.g., {@link Nullable}),
 *       ensuring that all fields are initialized before being used. This needs to be a separate
 *       checker because the target checker cannot access any type information from its parent,
 *       which is only initialized after its subcheckers have finished.
 *   <li>Override all necessary methods in the target checker's type factory to take the type
 *       information from the InitializationDeclarationChecker into account. You can look at {@link
 *       NullnessNoInitAnnotatedTypeFactory} for examples.
 * </ol>
 *
 * <p>If the command-line option {@code -AassumeInitialized} is given, this checker does nothing
 * except call its subcheckers. This gives users of, e.g., the NullnessChecker an easy way to turn
 * off initialization checking without having to directly call the NullnessNoInitSubchecker.
 *
 * <p>Note also that the flow-sensitive type refinement for this type system is performed by the
 * {@link InitializationFieldAccessChecker}; this checker performs no refinement, instead reusing
 * the results from that one.
 *
 * @checker_framework.manual #initialization-checker Initialization Checker
 */
@SupportedOptions({"assumeInitialized"})
public abstract class InitializationChecker extends BaseTypeChecker {

    /** Default constructor for InitializationChecker. */
    public InitializationChecker() {}

    /**
     * Whether to check primitives for initialization.
     *
     * @return whether to check primitives for initialization
     */
    public abstract boolean checkPrimitives();

    /**
     * The checker for the target type system for which to check initialization.
     *
     * @return the checker for the target type system.
     */
    public abstract Class<? extends BaseTypeChecker> getTargetCheckerClass();

    @Override
    public NavigableSet<String> getSuppressWarningsPrefixes() {
        NavigableSet<String> result = super.getSuppressWarningsPrefixes();
        // "fbc" is for backward compatibility only; you should use
        // "initialization" instead.
        result.add("fbc");
        // The default prefix "initialization" must be added manually because this checker class
        // is abstract and its subclasses are not named "InitializationChecker".
        result.add("initialization");
        return result;
    }

    @Override
    protected Set<Class<? extends BaseTypeChecker>> getImmediateSubcheckerClasses() {
        Set<Class<? extends BaseTypeChecker>> checkers = super.getImmediateSubcheckerClasses();
        checkers.add(getTargetCheckerClass());
        return checkers;
    }

    /** Returns a list of all fields of the given class. */
    public static List<VariableTree> getAllFields(ClassTree clazz) {
        List<VariableTree> fields = new ArrayList<>();
        for (Tree t : clazz.getMembers()) {
            if (t.getKind() == Tree.Kind.VARIABLE) {
                VariableTree vt = (VariableTree) t;
                fields.add(vt);
            }
        }
        return fields;
    }

    @Override
    public InitializationAnnotatedTypeFactory getTypeFactory() {
        return (InitializationAnnotatedTypeFactory) super.getTypeFactory();
    }

    @Override
    protected InitializationVisitor createSourceVisitor() {
        return new InitializationVisitor(this);
    }

    @Override
    protected boolean messageKeyMatches(
            String messageKey, String messageKeyInSuppressWarningsString) {
        // Also support the shorter keys used by typetools
        return super.messageKeyMatches(messageKey, messageKeyInSuppressWarningsString)
                || super.messageKeyMatches(
                        messageKey.replace(".invalid", ""), messageKeyInSuppressWarningsString);
    }
}
