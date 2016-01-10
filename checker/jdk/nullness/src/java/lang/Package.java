package java.lang;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.URL;

public class Package implements java.lang.reflect.AnnotatedElement{
  protected Package() {}
  // TODO: check signature
  Package(String name,
          String spectitle, String specversion, String specvendor,
          String impltitle, String implversion, String implvendor,
          URL sealbase, ClassLoader loader) {}

  public String getName() { throw new RuntimeException("skeleton method"); }
  public @Nullable String getSpecificationTitle() { throw new RuntimeException("skeleton method"); }
  public @Nullable String getSpecificationVersion() { throw new RuntimeException("skeleton method"); }
  public @Nullable String getSpecificationVendor() { throw new RuntimeException("skeleton method"); }
  public @Nullable String getImplementationTitle() { throw new RuntimeException("skeleton method"); }
  public @Nullable String getImplementationVersion() { throw new RuntimeException("skeleton method"); }
  public String getImplementationVendor() { throw new RuntimeException("skeleton method"); }
  @Pure public boolean isSealed() { throw new RuntimeException("skeleton method"); }
  @Pure public boolean isSealed(java.net.URL a1) { throw new RuntimeException("skeleton method"); }
  @Pure public boolean isCompatibleWith(String a1) throws NumberFormatException { throw new RuntimeException("skeleton method"); }
  @Pure public static @Nullable Package getPackage(String a1) { throw new RuntimeException("skeleton method"); }
  @Pure public static Package[] getPackages() { throw new RuntimeException("skeleton method"); }
  @Pure public static @Nullable Package getSystemPackage(String a1) { throw new RuntimeException("skeleton method"); }
  @Pure public static Package[] getSystemPackages() { throw new RuntimeException("skeleton method"); }
  @Pure public int hashCode() { throw new RuntimeException("skeleton method"); }
  @SideEffectFree public String toString() { throw new RuntimeException("skeleton method"); }
  public <A extends java.lang.annotation.Annotation> @Nullable A getAnnotation(Class<A> a1) { throw new RuntimeException("skeleton method"); }
  @Pure public boolean isAnnotationPresent(Class<? extends java.lang.annotation.Annotation> a1) { throw new RuntimeException("skeleton method"); }
  public java.lang.annotation.Annotation[] getAnnotations() { throw new RuntimeException("skeleton method"); }
  public java.lang.annotation.Annotation[] getDeclaredAnnotations() { throw new RuntimeException("skeleton method"); }
}
