package org.checkerframework.checker.initialization.qual;

import org.checkerframework.framework.qual.RelevantJavaTypes;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A meta-annotation that indicates that the qualifier can be applied to the default value of every
 * relevant java type (as per {@link RelevantJavaTypes}). It is used by the Initialization Checker
 * to know which fields declared without an initializer can still be considered initialized.
 *
 * <p>This meta-annotation should not be applied to the top qualifier in a hierarchy, as the top
 * qualifier must always respect this property by default. It should also not be applied to
 * monotonic or polymorphic qualifiers.
 *
 * <p>For example, the default value of every class-typed variable is {@code null}. Thus, in a
 * nullness types system, {@code Nullable} holds for default values (but should not be annotated
 * with this meta-annotation, since it is the top qualifier), but {@code NonNull} does not. For
 * another example, the default value for numerical primitive types is {@code 0}. Thus, in a type
 * system with qualifiers {@code Even}, {@code Odd}, {@code Unknown}, the top qualifier {@code
 * Unknown} holds for default values and should not be annotated with this meta-annotation, {@code
 * Even} holds for default values should be annotated, and {@code Odd} does not hold for default
 * values and should not be annotated.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface HoldsForDefaultValues {}
