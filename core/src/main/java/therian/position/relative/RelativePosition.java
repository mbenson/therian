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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;

import therian.TherianContext;
import therian.position.Position;

/**
 * {@link Position} relative to some other {@link Position.Readable}.
 *
 * @param <P>
 * @param <T>
 */
public interface RelativePosition<P, T> extends Position<T> {

    /**
     * Readable {@link RelativePosition}.
     *
     * @param <P>
     * @param <T>
     */
    public interface Readable<P, T> extends RelativePosition<P, T>, Position.Readable<T> {
    }

    /**
     * Writable {@link RelativePosition}.
     *
     * @param <P>
     * @param <T>
     */
    public interface Writable<P, T> extends RelativePosition<P, T>, Position.Writable<T> {
    }

    /**
     * Read/write {@link RelativePosition}.
     *
     * @param <P>
     * @param <T>
     */
    public interface ReadWrite<P, T> extends RelativePosition<P, T>, Position.ReadWrite<T> {
    }

    /**
     * Describes a {@link RelativePosition.Mixin}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Implements {
        @SuppressWarnings("rawtypes")
        Class<? extends Position>[] value();
    }

    /**
     * Uses an {@link Implements} annotation to declare the {@link Position} interface(s) for which it provides an
     * implementation, defining corresponding methods prepending the parent position as an argument.
     *
     * @param <T>
     */
    public interface Mixin<T> {
        public static final Helper HELPER = new Helper();

        public static class Helper {
            private Helper() {
            }

            public <T> Class<? extends Position<?>>[] getImplementedTypes(Mixin<T> mixin) {
                final HashSet<Class<? extends Position<?>>> set = new HashSet<Class<? extends Position<?>>>();
                addImplementedInterfacesTo(set, mixin.getClass());
                for (Class<?> iface : ClassUtils.getAllInterfaces(mixin.getClass())) {
                    addImplementedInterfacesTo(set, iface);
                }
                @SuppressWarnings("unchecked")
                final Class<? extends Position<?>>[] result = set.toArray(new Class[set.size()]);
                return result;
            }

            private static void addImplementedInterfacesTo(Set<Class<? extends Position<?>>> target, Class<?> type) {
                if (type == null) {
                    return;
                }
                final RelativePosition.Implements annotation = type.getAnnotation(RelativePosition.Implements.class);
                if (annotation != null) {
                    @SuppressWarnings("unchecked")
                    final Class<? extends Position<?>>[] classes = (Class<? extends Position<?>>[]) annotation.value();
                    Collections.addAll(target, classes);
                }
                addImplementedInterfacesTo(target, type.getSuperclass());
            }
        }

        @Implements(RelativePosition.class)
        public static class GetParentPosition<T> implements Mixin<T> {
            public <P> Position.Readable<? extends P> getParentPosition(Position.Readable<? extends P> parentPosition) {
                return parentPosition;
            }
        }

        public static class ELValue<TYPE> implements GetValue<TYPE>, SetValue<TYPE> {
            private final Object property;

            public ELValue(Object property) {
                super();
                this.property = property;
            }

            public <P> TYPE getValue(Position.Readable<? extends P> parentPosition) {
                final TherianContext context = TherianContext.getInstance();
                final Object value = context.getELResolver().getValue(context, parentPosition.getValue(), property);
                Validate.validState(context.isPropertyResolved(), "could not get value %s from %s", property,
                    parentPosition);
                @SuppressWarnings("unchecked")
                final TYPE result = (TYPE) value;
                return result;
            }

            public <P> void setValue(Position.Readable<? extends P> parentPosition, TYPE value) {
                final TherianContext context = TherianContext.getInstance();
                context.getELResolver().setValue(context, parentPosition.getValue(), property, value);
                Validate.validState(context.isPropertyResolved(), "could not set value %s onto %s from %s", value,
                    property, parentPosition);
            }

        }

        /**
         * Declaring this interface on a {@link RelativePosition.Mixin} will cause the result of a given
         * no-arg method invocation to be cached per {@link RelativePosition} proxy instance.
         */
        public interface Cacheable {
        }

    }

    /**
     * Get type relative to parent position.
     *
     * @param <T>
     */
    @Implements(Position.class)
    public interface GetType<T> extends Mixin<T> {
        <P> Type getType(Position.Readable<? extends P> parentPosition);
    }

    /**
     * Get value relative to parent position.
     *
     * @param <T>
     */
    @Implements(Position.Readable.class)
    public interface GetValue<T> extends Mixin<T> {
        <P> T getValue(Position.Readable<? extends P> parentPosition);
    }

    /**
     * Set value relative to parent position.
     *
     * @param <T>
     */
    @Implements(Position.Writable.class)
    public interface SetValue<T> extends Mixin<T> {
        <P> void setValue(Position.Readable<? extends P> parentValue, T value);
    }

    Position.Readable<? extends P> getParentPosition();
}
