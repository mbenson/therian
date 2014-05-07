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
package therian.position.relative;

import java.beans.FeatureDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.el.ELResolver;

import org.apache.commons.functor.Predicate;
import org.apache.commons.functor.core.collection.FilteredIterable;
import org.apache.commons.functor.generator.loop.IteratorToGeneratorAdapter;
import org.apache.commons.functor.generator.util.CollectionTransformer;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.TherianContext;
import therian.el.ELConstants;
import therian.position.Position;
import therian.util.Types;

/**
 * Fluent entry point for "property at" {@link RelativePositionFactory}.
 */
public class Property {
    private static final Logger LOG = LogManager.getLogManager().getLogger(Property.class.getName());

    private enum FeatureExtractionStrategy {
        GENERIC_TYPE_ATTRIBUTE {

            @Override
            Type getType(FeatureDescriptor feature) {
                return Type.class.cast(feature.getValue(ELConstants.GENERIC_TYPE));
            }
        },
        PROPERTY_DESCRIPTOR {

            @Override
            Type getType(FeatureDescriptor feature) {
                if (feature instanceof PropertyDescriptor) {
                    PropertyDescriptor pd = (PropertyDescriptor) feature;
                    Method readMethod = pd.getReadMethod();
                    if (readMethod != null) {
                        return readMethod.getGenericReturnType();
                    }
                    Method writeMethod = pd.getWriteMethod();
                    if (writeMethod != null) {
                        final int arg = pd instanceof IndexedPropertyDescriptor ? 1 : 0;
                        return writeMethod.getGenericParameterTypes()[arg];
                    }

                }
                return null;
            }
        },
        TYPE_ATTRIBUTE {

            @Override
            Type getType(FeatureDescriptor feature) {
                return Class.class.cast(feature.getValue(ELResolver.TYPE));
            }
        };
        abstract Type getType(FeatureDescriptor feature);
    }

    public static class PositionFactory<TYPE> extends RelativePositionFactory.ReadWrite<Object, TYPE> {

        private final String propertyName;
        private final boolean optional;

        private PositionFactory(final String propertyName) {
            this(propertyName, false);
        }

        private PositionFactory(final String propertyName, boolean optional) {
            this.propertyName = propertyName;
            this.optional = optional;
        }

        public String getPropertyName() {
            return propertyName;
        }

        @Override
        public <P> RelativePosition.ReadWrite<P, TYPE> of(Position.Readable<P> parentPosition) {
            class Result extends RelativePositionImpl<P, String> implements RelativePosition.ReadWrite<P, TYPE> {

                protected Result(therian.position.Position.Readable<P> parentPosition, String name) {
                    super(parentPosition, name);
                }

                @Override
                public Type getType() {
                    return Types.refine(getBasicType(), parentPosition.getType());
                }

                private Type getBasicType() {
                    final TherianContext context = TherianContext.getInstance();
                    final P parent = parentPosition.getValue();
                    final Predicate<FeatureDescriptor> filter = new Predicate<FeatureDescriptor>() {
                        public boolean test(FeatureDescriptor obj) {
                            return propertyName.equals(obj.getName());
                        }
                    };

                    Iterable<FeatureDescriptor> featureDescriptors = Collections.emptyList();
                    if (parent != null) {
                        try {
                            final Iterator<FeatureDescriptor> fd =
                                context.getELResolver().getFeatureDescriptors(context, parent);
                            featureDescriptors =
                                FilteredIterable.of(
                                    CollectionTransformer.<FeatureDescriptor> toCollection().evaluate(
                                        IteratorToGeneratorAdapter.adapt(fd))).retain(filter);
                        } catch (Exception e) {
                        }
                    }
                    for (FeatureDescriptor fd : featureDescriptors) {
                        final Type fromGenericTypeAttribute =
                            FeatureExtractionStrategy.GENERIC_TYPE_ATTRIBUTE.getType(fd);
                        if (fromGenericTypeAttribute != null) {
                            return fromGenericTypeAttribute;
                        }
                    }

                    final Type parentType = parentPosition.getType();
                    final Class<?> rawParentType = TypeUtils.getRawType(parentType, null);
                    try {
                        final List<PropertyDescriptor> beanPropertyDescriptors =
                            Arrays.asList(Introspector.getBeanInfo(rawParentType).getPropertyDescriptors());
                        for (PropertyDescriptor pd : FilteredIterable.of(beanPropertyDescriptors).retain(filter)) {
                            Type fromPropertyDescriptor = FeatureExtractionStrategy.PROPERTY_DESCRIPTOR.getType(pd);
                            if (fromPropertyDescriptor != null) {
                                return fromPropertyDescriptor;
                            }
                        }
                    } catch (IntrospectionException e) {
                        if (LOG.isLoggable(Level.WARNING)) {
                            LOG.log(Level.WARNING, String.format("Could not introspect %s", rawParentType), e);
                        }
                    }
                    for (FeatureDescriptor feature : featureDescriptors) {
                        Type fromTypeAttribute = FeatureExtractionStrategy.TYPE_ATTRIBUTE.getType(feature);
                        if (fromTypeAttribute != null) {
                            return fromTypeAttribute;
                        }
                    }
                    final Class<?> type =
                        context.getELResolver().getType(context, parentPosition.getValue(), propertyName);
                    Validate.validState(context.isPropertyResolved(), "could not resolve type of %s from %s",
                        propertyName, parentPosition);
                    return type;
                }

                @Override
                public TYPE getValue() {
                    final TherianContext context = TherianContext.getInstance();
                    final P parent = parentPosition.getValue();
                    final Object value = context.getELResolver().getValue(context, parent, propertyName);
                    if (context.isPropertyResolved()) {
                        @SuppressWarnings("unchecked")
                        final TYPE result = (TYPE) value;
                        return result;
                    }
                    if (optional && parent == null) {
                        return null;
                    }
                    throw new IllegalStateException(String.format("could not get value %s from %s", propertyName,
                        parentPosition));
                }
            }
            return new Result(parentPosition, propertyName);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            return obj instanceof PositionFactory && propertyName.equals(((PositionFactory<?>) obj).propertyName);
        }

        @Override
        public int hashCode() {
            return (71 << 4) | propertyName.hashCode();
        }

        @Override
        public String toString() {
            return String.format("Property %s", propertyName);
        }
    }

    /**
     * Create a {@link Property.PositionFactory} for the specified property.
     * 
     * @param propertyName
     * @return {@link PositionFactory}
     */
    public static <T> PositionFactory<T> at(String propertyName) {
        return new PositionFactory<T>(Validate.notEmpty(propertyName, "propertyName"));
    }

    /**
     * Create a {@link Property.PositionFactory} for an optional property. A position created from such a factory will
     * silently return {@code null} as its value if its parent's value is {@code null}.
     * 
     * @param propertyName
     * @return {@link PositionFactory}
     */
    public static <T> PositionFactory<T> optional(String propertyName) {
        final boolean optional = true;
        return new PositionFactory<T>(Validate.notEmpty(propertyName, "propertyName"), optional);
    }
}
