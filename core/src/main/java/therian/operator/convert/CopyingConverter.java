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
package therian.operator.convert;

import java.lang.reflect.Constructor;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.reflect.Typed;

import therian.BindTypeVariable;
import therian.Operation;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Convert;
import therian.operation.Copy;
import therian.position.Position;
import therian.util.Positions;
import therian.util.Types;

/**
 * Abstract base class for a converter that defers its work to a {@link Copy} {@link Operation}.
 */
public abstract class CopyingConverter<SOURCE, TARGET> extends Converter<SOURCE, TARGET> {
    /**
     * Class to be extended to implement dynamically typed {@link CopyingConverter}s.
     *
     * @param <SOURCE>
     * @param <TARGET>
     */
    public static abstract class DynamicallyTyped<SOURCE, TARGET> extends CopyingConverter<SOURCE, TARGET> {

        @BindTypeVariable
        public abstract Typed<SOURCE> getSourceType();

        @BindTypeVariable
        public abstract Typed<TARGET> getTargetType();

    }

    /**
     * Standard converter to {@link Iterable} (as {@link List}).
     */
    @SuppressWarnings("rawtypes")
    @StandardOperator
    public static final Converter<Object, Iterable> IMPLEMENTING_ITERABLE = CopyingConverter.implementing(
        Iterable.class).with(ArrayList.class);

    /**
     * Standard converter to {@link Collection} (as {@link List}).
     */
    @SuppressWarnings("rawtypes")
    @StandardOperator
    public static final Converter<Object, Collection> IMPLEMENTING_COLLECTION = CopyingConverter.implementing(
        Collection.class).with(ArrayList.class);

    /**
     * Standard converter to {@link List}.
     */
    @SuppressWarnings("rawtypes")
    @StandardOperator
    public static final Converter<Object, List> IMPLEMENTING_LIST = CopyingConverter.implementing(List.class).with(
        ArrayList.class);

    /**
     * Standard converter to {@link Set}.
     */
    @SuppressWarnings("rawtypes")
    @StandardOperator
    public static final Converter<Object, Set> IMPLEMENTING_SET = CopyingConverter.implementing(Set.class).with(
        LinkedHashSet.class);

    /**
     * Standard converter to {@link Map}.
     */
    @SuppressWarnings("rawtypes")
    @StandardOperator
    public static final Converter<Object, Map> IMPLEMENTING_MAP = CopyingConverter.implementing(Map.class).with(
        LinkedHashMap.class);

    /**
     * Standard converter to {@link SortedSet}.
     */
    @SuppressWarnings("rawtypes")
    @StandardOperator
    public static final Converter<Object, SortedSet> IMPLEMENTING_SORTED_SET = CopyingConverter.implementing(
        SortedSet.class).with(TreeSet.class);

    /**
     * Standard converter to {@link SortedMap}.
     */
    @SuppressWarnings("rawtypes")
    @StandardOperator
    public static final Converter<Object, SortedMap> IMPLEMENTING_SORTED_MAP = CopyingConverter.implementing(
        SortedMap.class).with(TreeMap.class);

    /**
     * Standard converter to {@link Queue}.
     */
    @SuppressWarnings("rawtypes")
    @StandardOperator
    public static final Converter<Object, Queue> IMPLEMENTING_QUEUE = CopyingConverter.implementing(Queue.class).with(
        ArrayDeque.class);

    /**
     * Standard converter to {@link Queue}.
     */
    @SuppressWarnings("rawtypes")
    @StandardOperator
    public static final Converter<Object, Deque> IMPLEMENTING_DEQUE = CopyingConverter.implementing(Deque.class).with(
        ArrayDeque.class);

    /**
     * Intermediate step in fluent interface. A {@link CopyingConverter} returned from {@link #with(Class)} imposes the
     * additional limitation that the conversion's target type be assignable to the converter's target type. this is
     * basically the reverse of the usual (and still observed) target assignability check of a {@link Converter}; the
     * net effect is that these will only kick in when their target type has been explicitly requested (notwithstanding
     * raw vs. parameterized).
     *
     * @param <TARGET>
     */
    public static class Implementing<TARGET> {
        private final Typed<TARGET> targetType;

        private Implementing(Typed<TARGET> targetType) {
            super();
            this.targetType = targetType;
        }

        public <C extends TARGET> CopyingConverter<Object, TARGET> with(final Class<C> concreteType) {
            return new Fluent<TARGET>(requireDefaultConstructor(concreteType)) {
                @Override
                public String toString() {
                    return super.toString() + " as " + concreteType.getName();
                }

                @Override
                public Typed<TARGET> getTargetType() {
                    return targetType;
                }

                @Override
                public boolean supports(TherianContext context, Convert<? extends Object, ? super TARGET> convert) {
                    return super.supports(context, convert)
                        && TypeUtils.isAssignable(convert.getTargetType().getType(), targetType.getType());
                }
            };
        }
    }

    private static abstract class Fluent<TARGET> extends CopyingConverter.DynamicallyTyped<Object, TARGET> {
        private static final Typed<Object> sourceType = TypeUtils.wrap(Object.class);

        private final Constructor<? extends TARGET> constructor;

        protected Fluent(Constructor<? extends TARGET> constructor) {
            super();
            this.constructor = constructor;
        }

        @Override
        protected TARGET createCopyDestination(Position.Readable<? extends Object> readable) throws Exception {
            return constructor.newInstance();
        }

        @Override
        public Typed<Object> getSourceType() {
            return sourceType;
        }

    }

    @Override
    public final boolean perform(final TherianContext context, final Convert<? extends SOURCE, ? super TARGET> convert) {
        final TARGET target;
        try {
            target = createCopyDestination(convert.getSourcePosition());
            // make result available to any concurrent equivalent conversions:
            convert.getTargetPosition().setValue(target);
        } catch (Exception e) {
            return false;
        }
        final Position.Readable<TARGET> targetPosition;
        if (convert.getTargetPosition() instanceof Position.Readable<?>) {
            // use readable target position directly in the copy delegate
            @SuppressWarnings("unchecked")
            final Position.Readable<TARGET> unchecked = (Position.Readable<TARGET>) convert.getTargetPosition();
            targetPosition = unchecked;
        } else {
            targetPosition = Positions.readOnly(convert.getTargetPosition().getType(), target);
        }
        return context.evalSuccess(Copy.to(targetPosition, convert.getSourcePosition()));
    }

    @Override
    public boolean supports(TherianContext context, Convert<? extends SOURCE, ? super TARGET> convert) {
        // ideally we would check whether the copy was possible, but a copier typically knows it can't copy to
        // an immutable target, including a null value, so we would have to instantiate the target object twice
        // or resort to weird ways of reusing it and even then it might not get used, so we'll just risk
        // failure to perform() instead. We do reject null source values, however.
        return super.supports(context, convert) && convert.getSourcePosition().getValue() != null;
    }

    /**
     * Create copy destination object from source position.
     *
     * @param readable object
     * @return TARGET
     */
    protected abstract TARGET createCopyDestination(Position.Readable<? extends SOURCE> readable) throws Exception;

    @Override
    public String toString() {
        return String.format("CopyingConverter for target type %s",
            TypeUtils.toString(Types.resolveAt(this, Converter.class.getTypeParameters()[1])));
    }

    private static <T> Constructor<T> requireDefaultConstructor(Class<T> type) {
        return Validate.notNull(ConstructorUtils.getAccessibleConstructor(type),
            "Could not find default constructor for %s", type);
    }

    /**
     * Create a {@link CopyingConverter} instance that instantiates the target type using the default constructor.
     *
     * @param targetType which must have an accessible no-arg constructor
     * @param <TARGET>
     * @return CopyingConverter instance
     */
    public static <TARGET> CopyingConverter<Object, TARGET> forTargetType(final Class<TARGET> target) {
        final Typed<TARGET> targetType = TypeUtils.wrap(target);

        return new Fluent<TARGET>(requireDefaultConstructor(target)) {
            @Override
            public Typed<TARGET> getTargetType() {
                return targetType;
            }
        };
    }

    /**
     * Intermediate step to create a {@link CopyingConverter} instance that instantiates the (most likely abstract)
     * target type using the default constructor of a specific implementation.
     *
     * @param targetType
     * @return {@link Implementing} step
     */
    public static <TARGET> Implementing<TARGET> implementing(Class<TARGET> targetType) {
        return new Implementing<TARGET>(TypeUtils.wrap(targetType));
    }

    /**
     * Intermediate step to create a {@link CopyingConverter} instance that instantiates the (most likely abstract)
     * target type using the default constructor of a specific implementation.
     *
     * @param targetType
     * @return {@link Implementing} step
     */
    public static <TARGET> Implementing<TARGET> implementing(Typed<TARGET> targetType) {
        return new Implementing<TARGET>(targetType);
    }
}
