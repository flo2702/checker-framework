package org.checkerframework.checker.initialization;

import org.checkerframework.common.basetype.BaseTypeChecker;

/**
 * Part of the freedom-before-commitment type system.
 *
 * <p>This checker does not actually do any type checking. It only exists to provide its parent
 * checker (the {@link InitializationChecker#getTargetCheckerClass()}) with declared initialization
 * qualifiers via the {@link
 * InitializationDeclarationAnnotatedTypeFactory.CommitmentFieldAccessTreeAnnotator}.
 *
 * @see InitializationChecker
 */
public class InitializationDeclarationChecker extends BaseTypeChecker {

    public InitializationDeclarationChecker() {}
}
