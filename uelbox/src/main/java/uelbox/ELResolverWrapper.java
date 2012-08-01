/*
 *  Copyright the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uelbox;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;

import org.apache.commons.lang3.Validate;

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
        this.wrapped = Validate.notNull(wrapped, "wrapped ELResolver");
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
