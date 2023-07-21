package org.checkerframework.checker.initialization;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

import org.checkerframework.checker.initialization.InitializationFieldAccessAnnotatedTypeFactory.CommitmentFieldAccessTreeAnnotator;
import org.checkerframework.checker.nullness.NonNullAnnotatedTypeFactory;
import org.checkerframework.checker.nullness.NonNullSubchecker;
import org.checkerframework.checker.nullness.NullnessChecker;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.GenericAnnotatedTypeFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.processing.SupportedOptions;

/**
 * //TODO: update this Tracks whether a value is initialized (all its fields are set), and checks
 * that values are initialized before being used. Implements the freedom-before-commitment scheme
 * for initialization, augmented by type frames.
 *
 * <p>To use this type system, it does not suffice to simply add it as a subchecker. You must ensure
 * that your parent checker hooks into the Initialization Checker at the following points. You can
 * look at the {@link NullnessChecker} for an example.
 *
 * <p>First, you should add the {@link CommitmentFieldAccessTreeAnnotator} as a tree annotator. This
 * annotator gives possibly uninitialized fields the top type(s) of your hierarchy, ensuring that
 * all fields are initialized before being used.
 *
 * <p>Second, the checker uses a simple definite-assignment analysis to check whether a constructor
 * initializes every field. However, this is prone to false positives as it neither considers that
 * some fields (e.g., nullable fields) don't have to be initialized, nor can it take into account
 * contract annotations like {@link EnsuresNonNull}. Therefore, it does not report most type errors
 * when it runs, instead saving them for the parent checker to either discharge or report. The
 * parent checker's visitor should call ... for every node. This method goes through the list of
 * fields that are possibly uninitialized at the node's location and uses the parent checker's type
 * factory to filter out the above cases. If no uninitialized fields remain after the filtering, the
 * error is discharged. If some fields are still uninitialized, the error is reported.
 *
 * <p>Third, you should override the following methods in the parent checker's type factory to take
 * the initialization type information into account. You can look at {@link
 * NonNullAnnotatedTypeFactory} for examples.
 *
 * <ol>
 *   <li>{@link GenericAnnotatedTypeFactory#isNotFullyInitializedReceiver}
 *   <li>{@link GenericAnnotatedTypeFactory#getAnnotatedTypeBefore}
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
