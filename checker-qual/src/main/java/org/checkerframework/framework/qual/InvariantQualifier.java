package org.checkerframework.framework.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A meta-annotation that indicates that a qualifier is an invariant.
 *
 * <p>Reading an field of an invariant type {@code T} might yield an expression of the top type
 * under certain conditions depending on the type system. However, once it has been observed that a
 * variable has the invariant type {@code T}, the invariant property ensures that it will stay of
 * type {@code T} for the rest of the program execution. This is even true if arbitrary other code
 * is executed.
 *
 * <p>This is mainly useful for type systems that use the Initialization Checker or something
 * similar.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface InvariantQualifier {}
