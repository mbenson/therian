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
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.el.ELResolver;

import org.apache.commons.collections4.IteratorUtils;
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
    private static final String THERIAN_PROPERTY_METHOD_WEAVER = "therian-property-method-weaver";

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

        Optional<Type> findType(Stream<? extends FeatureDescriptor> features) {
            return features.map(this::getType).filter(Objects::nonNull).findFirst();
        }
    }

    public static class PositionFactory<PARENT, TYPE> extends RelativePositionFactory.ReadWrite<PARENT, TYPE> {

        private final String propertyName;
        private final boolean optional;

        public PositionFactory(final String propertyName) {
            this(propertyName, false);
        }

        public PositionFactory(final String propertyName, boolean optional) {
            this.propertyName = Validate.notEmpty(propertyName, "propertyName");
            this.optional = optional;
        }

        public String getPropertyName() {
            return propertyName;
        }

        @Override
        public <P extends PARENT> RelativePosition.ReadWrite<P, TYPE> of(Position.Readable<P> parentPosition) {
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
                    final Predicate<FeatureDescriptor> featureMatchingName = d -> propertyName.equals(d.getName());

                    Stream<FeatureDescriptor> matchingFeatures = Stream.empty();
                    if (parent != null) {
                        try {
                            matchingFeatures =
                                IteratorUtils.toList(context.getELResolver().getFeatureDescriptors(context, parent))
                                    .stream().filter(featureMatchingName);

                            final Optional<Type> fromGenericTypeAttribute =
                                FeatureExtractionStrategy.GENERIC_TYPE_ATTRIBUTE.findType(matchingFeatures);
                            if (fromGenericTypeAttribute.isPresent()) {
                                return fromGenericTypeAttribute.get();
                            }
                        } catch (Exception e) {
                        }
                    }

                    final Type parentType = parentPosition.getType();
                    final Class<?> rawParentType = TypeUtils.getRawType(parentType, null);
                    try {
                        final Optional<Type> fromPropertyDescriptor =
                            FeatureExtractionStrategy.PROPERTY_DESCRIPTOR.findType(Arrays.stream(
                                Introspector.getBeanInfo(rawParentType).getPropertyDescriptors()).filter(
                                featureMatchingName));

                        if (fromPropertyDescriptor.isPresent()) {
                            return fromPropertyDescriptor.get();
                        }
                    } catch (IntrospectionException e) {
                        if (LOG.isLoggable(Level.WARNING)) {
                            LOG.log(Level.WARNING, String.format("Could not introspect %s", rawParentType), e);
                        }
                    }
                    final Optional<Type> fromTypeAttribute =
                        FeatureExtractionStrategy.TYPE_ATTRIBUTE.findType(matchingFeatures);
                    if (fromTypeAttribute.isPresent()) {
                        return fromTypeAttribute.get();
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
            return obj instanceof PositionFactory && propertyName.equals(((PositionFactory<?, ?>) obj).propertyName)
                && optional == ((PositionFactory<?, ?>) obj).optional;
        }

        @Override
        public int hashCode() {
            return Objects.hash(propertyName, optional);
        }

        @Override
        public String toString() {
            return String.format("%sProperty %s", optional ? "Optional " : "", propertyName);
        }
    }

    /**
     * Wraps a (lambda) reference to a property accessor(/getter) method. Intended not for direct use but rather with
     * {@link Property#at(Function)} and {@link Property#optional(Function)}. Wrapping accessor references in this type
     * means that they can survive being passed through other methods before reaching {@link Property}'s
     * {@link PositionFactory} methods.
     *
     * @param <P>
     * @param <T>
     */
    public static class Accessor<P, T> implements Function<P, T> {
        private final String propertyName;
        private final Function<? super P, ? extends T> wrapped;

        public Accessor(String propertyName, Function<P, T> wrapped) {
            super();
            this.propertyName = Validate.notBlank(propertyName, "propertyName was blank");
            this.wrapped = Validate.notNull(wrapped, "wrapped");
        }

        @Override
        public T apply(P parent) {
            return wrapped.apply(parent);
        }
    }

    /**
     * Wraps a (lambda) reference to a property mutator(/setter) method. Intended not for direct use but rather with
     * {@link Property#at(BiConsumer)} and {@link Property#optional(BiConsumer)}. Wrapping mutator references in this
     * type means that they can survive being passed through other methods before reaching {@link Property}'s
     * {@link PositionFactory} methods.
     *
     * @param <P>
     * @param <T>
     */
    public static class Mutator<P, T> implements BiConsumer<P, T> {
        private final String propertyName;
        private final BiConsumer<? super P, ? super T> wrapped;
        
        public Mutator(String propertyName, BiConsumer<? super P, ? super T> wrapped) {
            super();
            this.propertyName = Validate.notBlank(propertyName, "propertyName was blank");
            this.wrapped = Validate.notNull(wrapped, "wrapped");
        }

        @Override
        public void accept(P t, T u) {
            wrapped.accept(t, u);
        }
    }

    /**
     * Create a {@link Property.PositionFactory} for the specified property.
     *
     * @param propertyName
     * @return {@link PositionFactory}
     */
    public static <T> PositionFactory<Object, T> at(String propertyName) {
        return new PositionFactory<>(propertyName);
    }

    /**
     * Create a {@link Property.PositionFactory} for an optional property. A position created from such a factory will
     * silently return {@code null} as its value if its parent's value is {@code null}.
     *
     * @param propertyName
     * @return {@link PositionFactory}
     */
    public static <T> PositionFactory<Object, T> optional(String propertyName) {
        final boolean optional = true;
        return new PositionFactory<>(propertyName, optional);
    }

    /**
     * Create a {@link Property.PositionFactory} for the property specified by {@code accessor}, whose original source
     * form is expected to have been a method reference for a valid Java bean accessor method, in which case it is
     * required that the calling class be processed with the {@code therian-property-method-weaver} for Apache Commons
     * Weaver, which wraps the reference in an {@link Accessor} object. Alternatively the {@link Accessor} can be
     * explicitly instantiated.
     *
     * @param accessor
     * @return {@link PositionFactory}
     * @throws IllegalArgumentException if {@code accessor} is not an {@link Accessor} instance at runtime.
     */
    public static <P, T> PositionFactory<P, T> at(Function<? super P, ? extends T> accessor) {
        Validate.isInstanceOf(Accessor.class, accessor, "Cannot detect property from %s; missing %s?", accessor,
            THERIAN_PROPERTY_METHOD_WEAVER);
        return new PositionFactory<>(((Accessor<? super P, ? extends T>) accessor).propertyName);
    }

    /**
     * Create a {@link Property.PositionFactory} for the property specified by {@code mutator}, whose original source
     * form is expected to have been a method reference for a valid Java bean mutator method, in which case it is
     * required that the calling class be processed with the {@code therian-property-method-weaver} for Apache Commons
     * Weaver, which wraps the reference in a {@link Mutator} object. Alternatively the {@link Mutator} can be
     * explicitly instantiated.
     *
     * @param accessor
     * @return {@link PositionFactory}
     * @throws IllegalArgumentException if {@code accessor} is not an {@link Accessor} instance at runtime.
     */
    public static <P, T> PositionFactory<P, T> at(BiConsumer<? super P, ? super T> mutator) {
        Validate.isInstanceOf(Mutator.class, mutator, "Cannot detect property from %s; missing %s?", mutator,
            THERIAN_PROPERTY_METHOD_WEAVER);
        return new PositionFactory<>(((Mutator<? super P, ? super T>) mutator).propertyName);
    }

    /**
     * Create a {@link Property.PositionFactory} for the optional property specified by {@code accessor}, whose original
     * source form is expected to have been a method reference for a valid Java bean accessor method, in which case it
     * is required that the calling class be processed with the {@code therian-property-method-weaver} for Apache
     * Commons Weaver, which translates the method call. Alternatively the {@link Accessor} class can be explicitly
     * instantiated. A position created from such a factory will silently return {@code null} as its value if its
     * parent's value is {@code null}.
     *
     * @param accessor
     * @return {@link PositionFactory}
     * @throws IllegalArgumentException if {@code accessor} is not an {@link Accessor} instance.
     */
    public static <P, T> PositionFactory<P, T> optional(Function<? super P, ? extends T> accessor) {
        Validate.isInstanceOf(Accessor.class, accessor, "Cannot detect property from %s; missing %s?", accessor,
            THERIAN_PROPERTY_METHOD_WEAVER);
        final boolean optional = true;
        return new PositionFactory<>(((Accessor<? super P, ? extends T>) accessor).propertyName, optional);
    }

    /**
     * Create a {@link Property.PositionFactory} for the optional property specified by {@code mutator}, whose original
     * source form is expected to have been a method reference for a valid Java bean mutator method, in which case it is
     * required that the calling class be processed with the {@code therian-property-method-weaver} for Apache Commons
     * Weaver, which wraps the reference in a {@link Mutator} object. Alternatively the {@link Mutator} can be
     * explicitly instantiated. A position created from such a factory will silently return {@code null} as its value if
     * its parent's value is {@code null}.
     *
     * @param accessor
     * @return {@link PositionFactory}
     * @throws IllegalArgumentException if {@code accessor} is not an {@link Accessor} instance at runtime.
     */
    public static <P, T> PositionFactory<P, T> optional(BiConsumer<? super P, ? super T> mutator) {
        Validate.isInstanceOf(Mutator.class, mutator, "Cannot detect property from %s; missing %s?", mutator,
            THERIAN_PROPERTY_METHOD_WEAVER);
        final boolean optional = true;
        return new PositionFactory<>(((Mutator<? super P, ? super T>) mutator).propertyName, optional);
    }
}
