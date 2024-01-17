package org.checkerframework.checker.initialization;

import org.checkerframework.common.basetype.BaseTypeChecker;

/* NO-AFU
   import org.checkerframework.common.wholeprograminference.WholeProgramInference;
*/

/** The default visitor used by the freedom-before-commitment type-system. */
public class InitializationVisitor
        extends InitializationAbstractVisitor<InitializationAnnotatedTypeFactory> {

    /**
     * Create an InitializationVisitor.
     *
     * @param checker the initialization checker
     */
    public InitializationVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    protected InitializationAnnotatedTypeFactory createTypeFactory() {
        // Don't load the factory reflexively based on checker class name.
        // Instead, always use the InitializationAnnotatedTypeFactory.
        return new InitializationAnnotatedTypeFactory(checker);
    }
}
