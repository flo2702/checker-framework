package org.checkerframework.checker.initialization;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

import org.checkerframework.checker.initialization.InitializationFieldAccessAnnotatedTypeFactory.CommitmentFieldAccessTreeAnnotator;
import org.checkerframework.checker.nullness.NonNullAnnotatedTypeFactory;
import org.checkerframework.checker.nullness.NonNullSubchecker;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.qual.InvariantQualifier;

import java.util.ArrayList;
import java.util.Collection;
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
 * using this type system is more complex than others. To use this type system, the target checker
 * must:
 *
 * <ol>
 *   <li>Use this checker as its parent checker via {@link #SUBCHECKER_CLASS}. This is necessary
 *       because this checker is dependent on the target checker to know which fields should be
 *       checked for initialization, and when such a field is initialized: A field is checked for
 *       initialization if its declared type has an {@link InvariantQualifier}. Such a field becomes
 *       initialized when its refined type has that same invariant qualifier.
 *   <li>Use the {@link InitializationFieldAccessChecker} as a subchecker and add its {@link
 *       CommitmentFieldAccessTreeAnnotator} as a tree annotator. This is necessary to give possibly
 *       uninitialized fields the top type of the target hierarchy, ensuring that all fields are
 *       initialized before being used. This needs to be a separate checker because the target
 *       checker cannot access any type information from its parent checker, as the parent checker
 *       is only initialized after its children have finished.
 *   <li>Override all necessary methods in the target checker's type factory to take the type
 *       information from the InitializationFieldAccessChecker into account. You can look at {@link
 *       NonNullAnnotatedTypeFactory} for examples.
 * </ol>
 *
 * @checker_framework.manual #initialization-checker Initialization Checker
 */
@SupportedOptions({"assumeInitialized"})
public class InitializationChecker extends BaseTypeChecker {

    // TODO: In the future, this should be handled in a more dynamic way to make the
    // Initialization Checker reusable.
    /** The checker of the type system for which to check initialization. */
    public static final Class<? extends BaseTypeChecker> SUBCHECKER_CLASS = NonNullSubchecker.class;

    /** Whether to check primitives for initialization. */
    public static boolean CHECK_PRIMITIVES = false;

    /** Default constructor for InitializationChecker. */
    public InitializationChecker() {}

    @Override
    public NavigableSet<String> getSuppressWarningsPrefixes() {
        NavigableSet<String> result = super.getSuppressWarningsPrefixes();
        // "fbc" is for backward compatibility only; you should use
        // "initialization" instead.
        result.add("fbc");
        // TODO: This is for backward compatibility with projects that used
        // a previous version of the Nullness Checker which used the
        // Initialization Checker as a superclass instead of as a parent checker.
        // Perhaps it should be turned into a toggleable option.
        for (BaseTypeChecker subchecker : getSubcheckers()) {
            result.addAll(subchecker.getSuppressWarningsPrefixes());
        }
        return result;
    }

    @Override
    public Collection<String> getSuppressWarningsPrefixesOfSubcheckers() {
        return getSuppressWarningsPrefixes();
    }

    @Override
    protected Set<Class<? extends BaseTypeChecker>> getImmediateSubcheckerClasses() {
        Set<Class<? extends BaseTypeChecker>> checkers = super.getImmediateSubcheckerClasses();
        checkers.add(SUBCHECKER_CLASS);
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
    protected boolean messageKeyMatches(
            String messageKey, String messageKeyInSuppressWarningsString) {
        // Also support the shorter keys used by typetools
        return super.messageKeyMatches(messageKey, messageKeyInSuppressWarningsString)
                || super.messageKeyMatches(
                        messageKey.replace(".invalid", ""), messageKeyInSuppressWarningsString);
    }
}
