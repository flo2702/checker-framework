package org.checkerframework.checker.initialization;

import org.checkerframework.framework.flow.CFAbstractValue;

import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;

public class InitializationValue extends CFAbstractValue<InitializationValue> {

    protected InitializationValue(
            InitializationAnalysis analysis,
            Set<AnnotationMirror> annotations,
            TypeMirror underlyingType) {
        super(analysis, annotations, underlyingType);
    }
}
