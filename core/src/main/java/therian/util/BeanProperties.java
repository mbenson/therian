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
package therian.util;

import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.functor.generator.loop.IteratorToGeneratorAdapter;
import org.apache.commons.functor.generator.util.CollectionTransformer;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.TherianContext;
import therian.position.Position;

/**
 * Bean property utility methods.
 */
public class BeanProperties {
    private BeanProperties() {
    }

    public enum ReturnProperties {
        ALL, WRITABLE;
    }

    public static Set<String> getPropertyNames(TherianContext context, Position.Readable<?> position) {
        return getPropertyNames(ReturnProperties.ALL, context, position);
    }

    public static Set<String> getPropertyNames(ReturnProperties returnProperties, TherianContext context,
        Position.Readable<?> position) {

        Iterable<? extends FeatureDescriptor> descriptors;
        // first try ELResolver:

        try {
            descriptors =
                CollectionTransformer.<FeatureDescriptor> toCollection().evaluate(
                    IteratorToGeneratorAdapter.adapt(context.getELResolver().getFeatureDescriptors(context,
                        position.getValue())));
        } catch (Exception e) {
            descriptors = null;
        }

        if (descriptors == null || !descriptors.iterator().hasNext()) {
            // java.beans introspection; on RT type if available, else raw position type:
            final Class<?> beanType;
            if (position.getValue() == null) {
                beanType = TypeUtils.getRawType(position.getType(), null);
            } else {
                beanType = position.getValue().getClass();
            }
            try {
                descriptors = Arrays.asList(Introspector.getBeanInfo(beanType).getPropertyDescriptors());
            } catch (IntrospectionException e1) {
                return Collections.emptySet();
            }
        }

        final Set<String> result = new HashSet<String>();
        for (final FeatureDescriptor fd : descriptors) {
            final String name = fd.getName();
            if (returnProperties == ReturnProperties.WRITABLE) {
                try {
                    if (context.getELResolver().isReadOnly(context, position.getValue(), name)) {
                        continue;
                    }
                } catch (Exception e) {
                    // if we can't even _check_ for readOnly, assume not writable:
                    continue;
                }
            }
            result.add(name);
        }
        return result;
    }

}
