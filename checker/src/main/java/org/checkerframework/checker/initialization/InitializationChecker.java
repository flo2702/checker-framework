package org.checkerframework.checker.initialization;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

import org.checkerframework.checker.initialization.InitializationAnnotatedTypeFactory.CommitmentFieldAccessTreeAnnotator;
import org.checkerframework.checker.nullness.NullnessAnnotatedTypeFactory;
import org.checkerframework.checker.nullness.NullnessChecker;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.GenericAnnotatedTypeFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * Tracks whether a value is initialized (all its fields are set), and checks that values are
 * initialized before being used. Implements the freedom-before-commitment scheme for
 * initialization, augmented by type frames.
 *
 * <p>To use this type system, it does not suffice to simply add it as a subchecker. You must ensure
 * that your parent checker hooks into the init checker at the following points. You can look at the
 * {@link NullnessChecker} for an example.
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
 * parent checker's visitor should call {@link
 * InitializationAnnotatedTypeFactory#reportInitializionErrors} for every node. This method goes
 * through the list of fields that are possibly uninitialized at the node's location and uses the
 * parent checker's type factory to filter out the above cases. If no uninitialized fields remain
 * after the filtering, the error is discharged. If some fields are still uninitialized, the error
 * is reported.
 *
 * <p>Third, you should override the following methods in the parent checker's type factory to take
 * the initialization type information into account. You can look at {@link
 * NullnessAnnotatedTypeFactory} for examples.
 *
 * <ol>
 *   <li>{@link
 *       GenericAnnotatedTypeFactory#isNotFullyInitializedReceiver(com.sun.source.tree.MethodTree)}
 *   <li>{@link
 *       GenericAnnotatedTypeFactory#getAnnotatedTypeBefore(org.checkerframework.dataflow.expression.JavaExpression,
 *       Tree)}
 * </ol>
 *
 * @checker_framework.manual #initialization-checker Initialization Checker
 */
public class InitializationChecker extends BaseTypeChecker {

    @Override
    public SortedSet<String> getSuppressWarningsPrefixes() {
        SortedSet<String> result = super.getSuppressWarningsPrefixes();
        // "fbc" is for backward compatibility only; you should use
        // "initialization" instead.
        result.add("fbc");
        return result;
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
