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
package therian.uelbox;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ListELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

/**
 * Handles {@link Iterable} base objects in the manner of {@link ListELResolver}, but is always read-only and should not
 * be used as a fully featured replacement for that type.
 */
public class IterableELResolver extends ELResolver {

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return base instanceof Iterable<?> ? Integer.class : null;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) throws NullPointerException,
        PropertyNotFoundException, ELException {
        return seek(context, base, property) == null ? null : Object.class;
    }

    /**
     * Like {@link ListELResolver}, returns {@code null} for an illegal index.
     */
    @Override
    public Object getValue(ELContext context, Object base, Object property) throws NullPointerException,
        PropertyNotFoundException, ELException {
        
        Iterator<?> pos;
        try {
            pos = seek(context, base, property);
        } catch(PropertyNotFoundException e) {
            pos = null;
        }
        return pos == null ? null : pos.next();
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) throws NullPointerException,
        PropertyNotFoundException, ELException {
        // ignore in case someone else wants to take a crack at it
        return false;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) throws NullPointerException,
        PropertyNotFoundException, PropertyNotWritableException, ELException {
        // ignore in case someone else wants to take a crack at it
    }

    /**
     * Establishes an Iterator and advances it to the specified index. If this operation succeeds the context will be
     * set as having been resolved.
     * 
     * @param context
     * @param base
     * @param property
     * @return Iterator
     * @throws PropertyNotFoundException
     *             if index is out of bounds
     */
    private static Iterator<?> seek(ELContext context, Object base, Object property) {
        if (base instanceof Iterable<?>) {
            context.setPropertyResolved(true);
            int index = toIndex(context, property);
            if (index >= 0) {
                Iterator<?> result = ((Iterable<?>) base).iterator();
                for (int i = 0; i < index && result.hasNext(); i++) {
                    result.next();
                }
                if (result.hasNext()) {
                    return result;
                }
                throw new PropertyNotFoundException(String.valueOf(property));
            }
        }
        return null;
    }

    private static int toIndex(ELContext context, Object property) {
        try {
            return UEL.coerceToType(context, Integer.class, property).intValue();
        } catch (ELException e) {
            throw new IllegalArgumentException(String.valueOf(property));
        }
    }
}
