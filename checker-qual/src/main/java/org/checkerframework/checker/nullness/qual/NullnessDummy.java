package org.checkerframework.checker.nullness.qual;

import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Dummy qualifier for NullnessChecker, which does nothing. All the work is done by the subchecker,
 * the NonNullChecker, Initialization Checker, and KeyForChecker.
 *
 * @checker_framework.manual #nullness-checker Nullness Checker
 * @checker_framework.manual #initialization-checker Initialization Checker
 * @checker_framework.manual #bottom-type the bottom type
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@DefaultQualifierInHierarchy
@SubtypeOf({})
public @interface NullnessDummy {}
