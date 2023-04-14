package org.checkerframework.checker.nullness;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.dataflow.cfg.node.MethodInvocationNode;
import org.checkerframework.dataflow.cfg.visualize.CFGVisualizer;
import org.checkerframework.dataflow.expression.FieldAccess;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFAbstractStore;
import org.checkerframework.framework.type.AnnotatedTypeFactory;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * In addition to the base class behavior, tracks whether {@link PolyNull} is known to be {@link
 * NonNull} or {@link Nullable} (or not known to be either).
 */
public class NullnessStore extends CFAbstractStore<NullnessValue, NullnessStore> {

    /** True if, at this point, {@link PolyNull} is known to be {@link NonNull}. */
    protected boolean isPolyNullNonNull;

    /** True if, at this point, {@link PolyNull} is known to be {@link Nullable}. */
    protected boolean isPolyNullNull;

    /**
     * Create a NullnessStore.
     *
     * @param analysis the analysis class this store belongs to
     * @param sequentialSemantics should the analysis use sequential Java semantics (i.e., assume
     *     that only one thread is running at all times)?
     */
    public NullnessStore(
            CFAbstractAnalysis<NullnessValue, NullnessStore, ?> analysis,
            boolean sequentialSemantics) {
        super(analysis, sequentialSemantics);
        isPolyNullNonNull = false;
        isPolyNullNull = false;
    }

    /**
     * Create a NullnessStore (copy constructor).
     *
     * @param s a store to copy
     */
    public NullnessStore(NullnessStore s) {
        super(s);
        isPolyNullNonNull = s.isPolyNullNonNull;
        isPolyNullNull = s.isPolyNullNull;
    }

    @Override
    public NullnessStore leastUpperBound(NullnessStore other) {
        NullnessStore lub = super.leastUpperBound(other);
        lub.isPolyNullNonNull = isPolyNullNonNull && other.isPolyNullNonNull;
        lub.isPolyNullNull = isPolyNullNull && other.isPolyNullNull;
        return lub;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Additionally, the {@link NullnessStore} keeps all field values for non-null fields.
     */
    @Override
    public void updateForMethodCall(
            MethodInvocationNode n, AnnotatedTypeFactory atypeFactory, NullnessValue val) {
        NullnessAnnotatedTypeFactory factory = (NullnessAnnotatedTypeFactory) atypeFactory;
        // Remove non-null fields to avoid performance issue reported in #1438.
        Map<FieldAccess, NullnessValue> removedFields =
                fieldValues.entrySet().stream()
                        .filter(
                                e ->
                                        factory.getAnnotatedTypeLhs(e.getKey().getField())
                                                .hasAnnotation(factory.NONNULL))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        fieldValues.keySet().removeAll(removedFields.keySet());

        super.updateForMethodCall(n, atypeFactory, val);

        // Add non-null fields again.
        fieldValues.putAll(removedFields);
    }

    @Override
    protected boolean supersetOf(CFAbstractStore<NullnessValue, NullnessStore> o) {
        if (!(o instanceof NullnessStore)) {
            return false;
        }
        NullnessStore other = (NullnessStore) o;
        if ((other.isPolyNullNonNull != isPolyNullNonNull)
                || (other.isPolyNullNull != isPolyNullNull)) {
            return false;
        }
        return super.supersetOf(other);
    }

    @Override
    protected String internalVisualize(CFGVisualizer<NullnessValue, NullnessStore, ?> viz) {
        return super.internalVisualize(viz)
                + viz.getSeparator()
                + viz.visualizeStoreKeyVal("isPolyNullNonNull", isPolyNullNonNull)
                + viz.getSeparator()
                + viz.visualizeStoreKeyVal("isPolyNullNull", isPolyNullNull);
    }

    /**
     * Returns true if, at this point, {@link PolyNull} is known to be {@link NonNull}.
     *
     * @return true if, at this point, {@link PolyNull} is known to be {@link NonNull}
     */
    public boolean isPolyNullNonNull() {
        return isPolyNullNonNull;
    }

    /**
     * Set the value of whether, at this point, {@link PolyNull} is known to be {@link NonNull}.
     *
     * @param isPolyNullNonNull whether, at this point, {@link PolyNull} is known to be {@link
     *     NonNull}
     */
    public void setPolyNullNonNull(boolean isPolyNullNonNull) {
        this.isPolyNullNonNull = isPolyNullNonNull;
    }

    /**
     * Returns true if, at this point, {@link PolyNull} is known to be {@link Nullable}.
     *
     * @return true if, at this point, {@link PolyNull} is known to be {@link Nullable}
     */
    public boolean isPolyNullNull() {
        return isPolyNullNull;
    }

    /**
     * Set the value of whether, at this point, {@link PolyNull} is known to be {@link Nullable}.
     *
     * @param isPolyNullNull whether, at this point, {@link PolyNull} is known to be {@link
     *     Nullable}
     */
    public void setPolyNullNull(boolean isPolyNullNull) {
        this.isPolyNullNull = isPolyNullNull;
    }
}
