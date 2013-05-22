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
import java.util.Collections;
import java.util.List;

import org.apache.commons.functor.UnaryPredicate;
import org.apache.commons.functor.core.collection.FilteredIterable;
import org.apache.commons.functor.generator.IteratorToGeneratorAdapter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.TherianContext;
import therian.el.ELConstants;
import therian.operation.Add;
import therian.position.Position;
import therian.uelbox.IterableELResolver;
import therian.util.Types;

/**
 * Provides fluent access to {@link RelativePositionFactory} instances for:
 * <ul>
 * <li>array elements</li>
 * <li> {@link List} elements</li>
 * <li>{@link Iterable} elements (requires {@link IterableELResolver})</li>
 * </ul>
 * Because this implementation uses unified EL facilities, growing lists/collections is not supported.
 *
 * @see Add
 */
public class Element {
    public static abstract class PositionFactory<PARENT, TYPE> extends RelativePositionFactory<PARENT, TYPE> {

        private final int index;

        private PositionFactory(int index) {
            this.index = index;
        }

        @Override
        public <P extends PARENT> RelativePosition.ReadWrite<P, TYPE> of(Position.Readable<P> parentPosition) {
            class Result extends RelativePositionImpl<P, Integer> implements RelativePosition.ReadWrite<P, TYPE> {

                Result(Position.Readable<P> parentPosition, int index) {
                    super(parentPosition, Integer.valueOf(index));
                }

                @Override
                public Type getType() {
                    return Types.refine(getBasicType(), parentPosition.getType());
                }

                private Type getBasicType() {
                    final TherianContext context = TherianContext.getInstance();
                    final P parent = parentPosition.getValue();
                    final UnaryPredicate<FeatureDescriptor> filter = new UnaryPredicate<FeatureDescriptor>() {
                        public boolean test(FeatureDescriptor obj) {
                            return Integer.toString(index).equals(obj.getName());
                        }
                    };

                    final Iterable<FeatureDescriptor> featureDescriptors =
                        parent == null ? Collections.<FeatureDescriptor> emptyList() : FilteredIterable.of(
                            IteratorToGeneratorAdapter.adapt(context.getELResolver().getFeatureDescriptors(context, parent))
                                .toCollection()).retain(filter);

                    for (FeatureDescriptor feature : featureDescriptors) {
                        final Type fromGenericTypeAttribute = Type.class.cast(feature.getValue(ELConstants.GENERIC_TYPE));
                        if (fromGenericTypeAttribute != null) {
                            return fromGenericTypeAttribute;
                        }
                    }

                    return ObjectUtils.defaultIfNull(evaluateElementType(parentPosition), Object.class);
                }            }
            return new Result(parentPosition, index);
        }

        protected abstract <P> Type evaluateElementType(Position<P> parentPosition);

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof PositionFactory == false) {
                return false;
            }
            return ((PositionFactory<?, ?>) obj).index == index;
        }

        @Override
        public int hashCode() {
            return 71 << 4 | index;
        }

        /**
         * Get the index
         * @return int
         */
        public int getIndex() {
            return index;
        }
    }

    public static <T> PositionFactory<Object, T> atArrayIndex(final int index) {
        final PositionFactory<Object, T> result = new PositionFactory<Object, T>(index) {
            @Override
            public <P> RelativePosition.ReadWrite<P, T> of(Position.Readable<P> parentPosition) {
                final Type parentType = parentPosition.getType();
                Validate.isTrue(TypeUtils.isArrayType(parentType), "%s is not an array type", parentType);
                return super.of(parentPosition);
            }

            @Override
            protected <P> Type evaluateElementType(Position<P> parentPosition) {
                return TypeUtils.getArrayComponentType(parentPosition.getType());
            }

            @Override
            public String toString() {
                return String.format("Array Element [%s]", index);
            }

        };
        return result;
    }

    public static <T> PositionFactory<Iterable<? extends T>, T> atIndex(final int index) {
        final PositionFactory<Iterable<? extends T>, T> result =
            new PositionFactory<Iterable<? extends T>, T>(index) {
                @Override
                public String toString() {
                    return String.format("Element [%s]", index);
                }

                @Override
                protected <P> Type evaluateElementType(Position<P> parentPosition) {
                    return TypeUtils.getTypeArguments(parentPosition.getType(), Iterable.class).get(
                        Iterable.class.getTypeParameters()[0]);
                }
            };
        return result;
    }
}
