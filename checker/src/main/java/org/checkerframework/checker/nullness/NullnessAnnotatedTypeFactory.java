package org.checkerframework.checker.nullness;

import org.checkerframework.checker.nullness.qual.NullnessDummy;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;

import java.lang.annotation.Annotation;
import java.util.Set;

public class NullnessAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    public NullnessAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        postInit();
    }

    @Override
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        return Set.of(NullnessDummy.class);
    }
}
