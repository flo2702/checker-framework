package viewpointtest;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AbstractViewpointAdapter;

import java.lang.annotation.Annotation;
import java.util.Set;

import viewpointtest.quals.*;

public class ViewpointTestAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    public ViewpointTestAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        this.postInit();
    }

    @Override
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        return getBundledTypeQualifiers(
                A.class,
                B.class,
                C.class,
                Bottom.class,
                PolyVP.class,
                ReceiverDependentQual.class,
                Top.class);
    }

    @Override
    protected AbstractViewpointAdapter createViewpointAdapter() {
        return new ViewpointTestViewpointAdapter(this);
    }
}
