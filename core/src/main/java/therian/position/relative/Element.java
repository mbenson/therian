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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.el.ELResolver;

import org.apache.commons.functor.UnaryPredicate;
import org.apache.commons.functor.core.collection.FilteredIterable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.TherianContext;
import therian.el.ELConstants;
import therian.position.Position;
import therian.util.Types;

/**
 * Provides fluent access to {@link RelativePositionFactory} instances for:
 * <ul>
 * <li>array elements</li>
 * <li> {@link List} elements</li>
 * <li>{@link Iterable} elements (requires TODO IterableELResolver)</li>
 * </ul>
 * Does not handle growing lists/collections; probably this functionality will be implemented elsewhere/how.
 */
public class Element {
    public static class ElementFactory<PARENT, TYPE> extends RelativePositionFactory<PARENT, TYPE> {

        private static abstract class GetTypeMixin<T> implements RelativePosition.GetType<T> {
            enum FeatureExtractionStrategy {
                GENERIC_TYPE_ATTRIBUTE {

                    @Override
                    Type getType(FeatureDescriptor feature) {
                        return Type.class.cast(feature.getValue(ELConstants.GENERIC_TYPE));
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

            final int index;

            GetTypeMixin(int index) {
                super();
                this.index = index;
            }

            public <P> Type getType(final Position.Readable<? extends P> parentPosition) {
                return Types.refine(getBasicType(parentPosition), parentPosition.getType());
            }

            private <P> Type getBasicType(final Position.Readable<? extends P> parentPosition) {
                final TherianContext context = TherianContext.getInstance();
                final P parent = parentPosition.getValue();
                final UnaryPredicate<FeatureDescriptor> filter = new UnaryPredicate<FeatureDescriptor>() {
                    public boolean test(FeatureDescriptor obj) {
                        return Integer.toString(index).equals(obj.getName());
                    }
                };

                final Iterable<FeatureDescriptor> featureDescriptors =
                    parent == null ? Collections.<FeatureDescriptor> emptyList() : FilteredIterable.of(
                        cache(context.getELResolver().getFeatureDescriptors(context, parent))).retain(filter);

                for (FeatureDescriptor feature : featureDescriptors) {
                    Type fromGenericTypeAttribute = FeatureExtractionStrategy.GENERIC_TYPE_ATTRIBUTE.getType(feature);
                    if (fromGenericTypeAttribute != null) {
                        return fromGenericTypeAttribute;
                    }
                }

                Type elementTypeOfParent = evaluateElementType(parentPosition);
                if (elementTypeOfParent != null) {
                    return elementTypeOfParent;
                }
                // TODO do we ever get here?
                for (FeatureDescriptor feature : featureDescriptors) {
                    Type fromTypeAttribute = FeatureExtractionStrategy.TYPE_ATTRIBUTE.getType(feature);
                    if (fromTypeAttribute != null) {
                        return fromTypeAttribute;
                    }
                }
                final Class<?> type = context.getELResolver().getType(context, parentPosition.getValue(), index);
                Validate.validState(context.isPropertyResolved(), "could not resolve type of %s from %s", index,
                    parentPosition);
                return type;
            }

            private <FD extends FeatureDescriptor> Iterable<FD> cache(Iterator<FD> iterator) {
                final ArrayList<FD> result = new ArrayList<FD>();
                while (iterator.hasNext()) {
                    result.add(iterator.next());
                }
                return result;
            }

            protected abstract <P> Type evaluateElementType(Position<P> parentPosition);
        }

        private final int index;

        private ElementFactory(final int index, RelativePosition.Mixin<TYPE>... mixins) {
            super(ArrayUtils.add(mixins, new RelativePosition.Mixin.ELValue<TYPE>(index)));
            this.index = index;
        }

        @Override
        public <P extends PARENT> RelativePosition.ReadWrite<P, TYPE> of(Position.Readable<P> parentPosition) {
            return (RelativePosition.ReadWrite<P, TYPE>) super.of(parentPosition);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof ElementFactory == false) {
                return false;
            }
            return ((ElementFactory<?, ?>) obj).index == index;
        }

        @Override
        public int hashCode() {
            return (71 << 4) | index;
        }
    }

    public static <T> ElementFactory<Object, T> atArrayIndex(int index) {
        @SuppressWarnings("unchecked")
        final ElementFactory<Object, T> result =
            new ElementFactory<Object, T>(index, new ElementFactory.GetTypeMixin<T>(index) {

                @Override
                protected <P> Type evaluateElementType(Position<P> parentPosition) {
                    return TypeUtils.getArrayComponentType(parentPosition.getType());
                }
            }) {
                @Override
                public <P> RelativePosition.ReadWrite<P, T> of(Position.Readable<P> parentPosition) {
                    final Type parentType = parentPosition.getType();
                    Validate.isTrue(TypeUtils.isArrayType(parentType), "%s is not an array type", parentType);
                    return super.of(parentPosition);
                }
            };
        return result;
    }

    public static <T> ElementFactory<Iterable<? extends T>, T> atIndex(int index) {
        @SuppressWarnings("unchecked")
        final ElementFactory<Iterable<? extends T>, T> result =
            new ElementFactory<Iterable<? extends T>, T>(index, new ElementFactory.GetTypeMixin<T>(index) {

                @Override
                protected <P> Type evaluateElementType(Position<P> parentPosition) {
                    return ObjectUtils.defaultIfNull(
                        TypeUtils.getTypeArguments(parentPosition.getType(), Iterable.class).get(
                            Iterable.class.getTypeParameters()[0]), Object.class);
                }

            });
        return result;
    }
}
