package org.checkerframework.framework.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A meta-annotation that indicates that a qualifier is an invariant.
 *
 * <p>An invariant is a qualifier {@code T} such that a reading field {@code o.f} declared as {@code
 * T} may not result in a value having the top qualifier instead of the qualifier {@code T} if
 * {@code o} is not initialized. (Note that thus the top qualifier of a hierarchy can never be an
 * invariant.) However, once it has been observed that a variable has the invariant type {@code T},
 * the invariant property ensures that it will stay of type {@code T} for the rest of the program
 * execution, even if arbitrary other code is executed. Though it may also be refined to a subtype
 * of {@code T}.
 *
 * <p>This is mainly useful for type systems that use the Initialization Checker or something
 * similar.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface InvariantQualifier {}
