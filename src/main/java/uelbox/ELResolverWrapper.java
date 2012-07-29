package uelbox;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;

/**
 * Wraps an {@link ELResolver}.
 */
public class ELResolverWrapper extends ELResolver {
    protected final ELResolver wrapped;

    /**
     * Create a new ELResolverWrapper.
     * 
     * @param wrapped
     */
    public ELResolverWrapper(ELResolver wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return wrapped.getCommonPropertyType(context, base);
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return wrapped.getFeatureDescriptors(context, base);
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        return wrapped.getType(context, base, property);
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        return wrapped.getValue(context, base, property);
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return wrapped.isReadOnly(context, base, property);
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        wrapped.setValue(context, base, property, value);
    }
}
