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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ClassUtils.Interfaces;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.reflect.Typed;

import therian.TherianContext;
import therian.TherianContext.Callback;
import therian.position.AbstractPosition;
import therian.position.Position;
import therian.position.Position.Readable;
import therian.position.Position.Writable;

/**
 * Utility methods relating to {@link Position}s.
 */
public class Positions {

    private static class RO<T> extends AbstractPosition.Readable<T> {
        private final Type type;
        private final T value;
        private final boolean isArray;

        RO(Type type, T value) {
            this.type = type;
            this.value = value;
            this.isArray = TypeUtils.isArrayType(type);
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("Read-Only Position<%s>(%s)", Types.toString(type),
                isArray ? ArrayUtils.toString(value, "null") : value);
        }

    }

    private static class W<T> extends AbstractPosition.Writable<T> {
        private final Type type;

        W(Type type) {
            this.type = type;
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public void setValue(T value) {
        }

        @Override
        public String toString() {
            return String.format("Writable Position<%s>", Types.toString(type));
        }

    }

    private static class RW<T> implements Position.ReadWrite<T> {
        private final Type type;
        private final boolean isArray;
        private T value;

        RW(Type type) {
            this.type = type;
            this.isArray = TypeUtils.isArrayType(type);
        }

        RW(Type type, T value) {
            this(type);
            setValue(value);
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public void setValue(T value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public String toString() {
            return String.format("Read-Write Position<%s>(%s)", Types.toString(type),
                isArray ? ArrayUtils.toString(value, "null") : value);
        }

    }

    /**
     * Learn whether {@code pos} is {@link Readable}.
     * 
     * @param pos
     * @return boolean
     */
    public static boolean isReadable(Position<?> pos) {
        return pos instanceof Position.Readable<?>;
    }

    /**
     * Learn whether {@code pos} is {@link Writable}.
     * 
     * @param pos
     * @return boolean
     */
    public static boolean isWritable(Position<?> pos) {
        return pos instanceof Position.Writable<?>;
    }

    /**
     * Get a read-only position of value {@code value} (type of {@code value#getClass()}.
     * 
     * @param value not {@code null}
     * @return Position.Readable
     */
    public static <T> Position.Readable<T> readOnly(final T value) {
        return readOnly(Validate.notNull(value, "value").getClass(), value);
    }

    /**
     * Get a read-only position of type {@code type} and value {@code value}.
     * 
     * @param type not {@code null}
     * @param value
     * @return Position.Readable
     */
    public static <T> Position.Readable<T> readOnly(final Type type, final T value) {
        Validate.notNull(type, "type");
        Validate.isTrue(TypeUtils.isInstance(value, type), "%s is not an instance of %s", value, Types.toString(type));
        return new RO<T>(type, value);
    }

    /**
     * Get a read-only position of type {@code type} and value {@code value}.
     * 
     * @param type not {@code null}
     * @param value
     * @return Position.Readable
     */
    public static <T> Position.Readable<T> readOnly(final Class<T> type, final T value) {
        return Positions.<T> readOnly((Type) type, value);
    }

    /**
     * Get a read-only position of type {@code type#value} and value {@code value}.
     * 
     * @param typed not {@code null}
     * @param value
     * @return Position.Readable
     */
    public static <T> Position.Readable<T> readOnly(final Typed<T> typed, final T value) {
        return readOnly(Validate.notNull(typed, "type").getType(), value);
    }

    /**
     * Get a read-write position of type {@code type}. No checking can be done to ensure that {@code type} conforms to
     * {@code T}.
     * 
     * @param type not {@code null}
     * @return Position.ReadWrite
     */
    public static <T> Position.ReadWrite<T> readWrite(final Type type) {
        Validate.notNull(type, "type");
        return new RW<T>(type);
    }

    /**
     * Get a read-write position of type {@code type}.
     * 
     * @param type not {@code null}
     * @return Position.ReadWrite
     */
    public static <T> Position.ReadWrite<T> readWrite(final Class<T> type) {
        return Positions.<T> readWrite((Type) type);
    }

    /**
     * Get a read-write position of type {@code type#value}.
     * 
     * @param typed not {@code null}
     * @return Position.ReadWrite
     */
    public static <T> Position.ReadWrite<T> readWrite(final Typed<T> typed) {
        return readWrite(Validate.notNull(typed.getType(), "type"));
    }

    /**
     * Get a read-write position of type {@code type} and with initial value {@code initialValue}.
     * 
     * @param type not {@code null}
     * @param initialValue
     * @return Position.ReadWrite
     */
    public static <T> Position.ReadWrite<T> readWrite(final Type type, T initialValue) {
        Validate.notNull(type, "type");
        Validate.isTrue(TypeUtils.isInstance(initialValue, type), "%s is not an instance of %s", initialValue,
            Types.toString(type));
        return new RW<T>(type, initialValue);
    }

    /**
     * Get a read-write position of type {@code type} and with initial value {@code initialValue}.
     * 
     * @param type not {@code null}
     * @param initialValue
     * @return Position.ReadWrite
     */
    public static <T> Position.ReadWrite<T> readWrite(final Class<T> type, T initialValue) {
        return Positions.<T> readWrite((Type) type, initialValue);
    }

    /**
     * Get a read-write position of type {@code type#value} and with initial value {@code initialValue}.
     * 
     * @param typed not {@code null}
     * @param initialValue
     * @return Position.ReadWrite
     */
    public static <T> Position.ReadWrite<T> readWrite(final Typed<T> typed, T initialValue) {
        return readWrite(Validate.notNull(typed, "type").getType(), initialValue);
    }

    /**
     * Get a writable position of type {@code type}. No checking can be done to ensure that {@code type} conforms to
     * {@code T}.
     * 
     * @param type not {@code null}
     * @return Position.Writable
     */
    public static <T> Position.Writable<T> writable(final Type type) {
        Validate.notNull(type, "type");
        return new W<T>(type);
    }

    /**
     * Get a writable position of type {@code type#value}.
     * 
     * @param typed not {@code null}
     * @return Position.Writable
     */
    public static <T> Position.Writable<T> writable(final Typed<T> typed) {
        return writable(Validate.notNull(typed, "type").getType());
    }

    /**
     * Get a writable position of type {@code type}.
     * 
     * @param type not {@code null}
     * @return Position.Writable
     */
    public static <T> Position.Writable<T> writable(final Class<T> type) {
        return Positions.<T> readWrite((Type) type);
    }

    /**
     * Get a UnaryProcedure callback for writing a position value.
     * 
     * @param pos
     * @return UnaryProcedure
     * @see TherianContext#forwardTo(therian.Operation, Callback)
     */
    public static <T> Callback<T> writeValue(final Position.Writable<? super T> pos) {
        return new Callback<T>() {

            @Override
            public void handle(T value) {
                pos.setValue(value);
            }
        };
    }

    /**
     * Get the narrowest parameterized type from the specified readable Position.
     * 
     * @param pos
     * @return Type
     */
    public static Type narrowestParameterizedType(Position.Readable<?> pos) {
        Type result = pos.getType();

        if (result instanceof ParameterizedType && pos.getValue() != null) {
            final Class<?> rawType = TypeUtils.getRawType(result, null);
            final Class<?> rt = pos.getValue().getClass();

            hierarchy: for (Class<?> t : ClassUtils.hierarchy(rt, Interfaces.INCLUDE)) {
                if (t.equals(rawType)) {
                    break;
                }
                if (!rawType.isAssignableFrom(t)) {
                    continue;
                }
                if (t.getTypeParameters().length > 0) {
                    // try to parameterize raw runtime type:
                    try {
                        final Map<TypeVariable<?>, Type> typeArgMappings =
                            TypeUtils.determineTypeArguments(t, (ParameterizedType) result);
                        result = TypeUtils.parameterize(t, typeArgMappings);
                    } catch (Exception e) {
                        // use basic parameterized source type
                    }
                    break;
                } else {
                    final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(rt, rawType);
                    for (TypeVariable<?> v : rawType.getTypeParameters()) {
                        final Type arg = typeArguments.get(v);
                        if (arg == null || arg instanceof TypeVariable<?>) {
                            continue hierarchy;
                        }
                    }
                    // if we are here, we have determined that rt explicitly binds all type params of the declared type
                    // of pos:
                    result = t;
                    break;
                }
            }
        }
        return result;
    }

    private Positions() {
    }

}
