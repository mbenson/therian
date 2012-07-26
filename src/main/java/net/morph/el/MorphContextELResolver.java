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
package net.morph.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;

/**
 * Special ELResolver that:
 * <ul>
 * <li>Helps implement operators</li>
 * <li>Provides our conversion facilities to
 * {@link #setValue(javax.el.ELContext, Object, Object, Object)}</li>
 * </ul>
 */
// TODO review responsibilities and corresponding doco of this class, or make
// private to MorphContext
public class MorphContextELResolver extends ELResolver {
    private final ELResolver delegate;

    public MorphContextELResolver(ELResolver delegate) {
        this.delegate = delegate;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return delegate.getCommonPropertyType(context, base);
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        // TODO cache/key? perhaps PropertyDescriptors only? that could work.
        // maybe via a different API
        return delegate.getFeatureDescriptors(context, base);
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        // TODO be smarter than the delegate
        return delegate.getType(context, base, property);
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        // TODO record stuff
        return delegate.getValue(context, base, property);
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return delegate.isReadOnly(context, base, property);
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        // TODO possibly record stuff?, also force conversions through our
        // facilities
        delegate.setValue(context, base, property, value);
    }
}
