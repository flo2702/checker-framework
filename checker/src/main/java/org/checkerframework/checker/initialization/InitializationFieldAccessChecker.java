package org.checkerframework.checker.initialization;

import org.checkerframework.common.basetype.BaseTypeChecker;

/**
 * Part of the freedom-before-commitment type system.
 *
 * <p>This checker does not actually do any type checking. It exists to provide its parent checker
 * (the {@link InitializationChecker#getTargetCheckerClass()}) with declared initialization
 * qualifiers via the {@link
 * InitializationFieldAccessAnnotatedTypeFactory.CommitmentFieldAccessTreeAnnotator}.
 *
 * <p>Additionally, this checker performs the flow-sensitive type refinement for the fbc type
 * system, which is necessary to avoid reporting follow-up errors related to initialization (see the
 * AssignmentDuringInitialization test case). To avoid performing the same type refinement twice,
 * the InitializationChecker performs no refinement, instead reusing the results from this checker.
 *
 * @see InitializationChecker
 */
public class InitializationFieldAccessChecker extends BaseTypeChecker {

    public InitializationFieldAccessChecker() {}
}
