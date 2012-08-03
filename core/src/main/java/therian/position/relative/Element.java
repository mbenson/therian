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
    public static class PositionFactory<PARENT, TYPE> extends RelativePositionFactory<PARENT, TYPE> {

        private final int index;

        private PositionFactory(final int index, RelativePosition.Mixin<TYPE>... mixins) {
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
            if (obj instanceof PositionFactory == false) {
                return false;
            }
            return ((PositionFactory<?, ?>) obj).index == index;
        }

        @Override
        public int hashCode() {
            return (71 << 4) | index;
        }
    }

    private static abstract class GetTypeMixin<T> implements RelativePosition.GetType<T> {
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
                    IteratorToGeneratorAdapter.adapt(context.getELResolver().getFeatureDescriptors(context, parent))
                        .toCollection()).retain(filter);

            for (FeatureDescriptor feature : featureDescriptors) {
                final Type fromGenericTypeAttribute = Type.class.cast(feature.getValue(ELConstants.GENERIC_TYPE));
                if (fromGenericTypeAttribute != null) {
                    return fromGenericTypeAttribute;
                }
            }

            return ObjectUtils.defaultIfNull(evaluateElementType(parentPosition), Object.class);
        }

        protected abstract <P> Type evaluateElementType(Position<P> parentPosition);
    }

    public static <T> PositionFactory<Object, T> atArrayIndex(int index) {
        @SuppressWarnings("unchecked")
        final PositionFactory<Object, T> result = new PositionFactory<Object, T>(index, new GetTypeMixin<T>(index) {

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

    public static <T> PositionFactory<Iterable<? extends T>, T> atIndex(int index) {
        @SuppressWarnings("unchecked")
        final PositionFactory<Iterable<? extends T>, T> result =
            new PositionFactory<Iterable<? extends T>, T>(index, new GetTypeMixin<T>(index) {

                @Override
                protected <P> Type evaluateElementType(Position<P> parentPosition) {
                    return TypeUtils.getTypeArguments(parentPosition.getType(), Iterable.class).get(
                        Iterable.class.getTypeParameters()[0]);
                }

            });
        return result;
    }
}
