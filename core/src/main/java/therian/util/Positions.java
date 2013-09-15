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

import java.lang.reflect.Type;

import org.apache.commons.functor.Procedure;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.TherianContext;
import therian.TypeLiteral;
import therian.position.Position;
import therian.position.Position.Readable;
import therian.position.Position.Writable;

/**
 * Utility methods relating to {@link Position}s.
 */
public class Positions {

    private static class RO<T> implements Position.Readable<T> {
        private final Type type;
        private final T value;

        RO(Type type, T value) {
            this.type = type;
            this.value = value;
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
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof RO == false) {
                return false;
            }
            RO<?> other = (RO<?>) obj;
            return other.getType().equals(type) && ObjectUtils.equals(other.getValue(), value);
        }

        @Override
        public int hashCode() {
            int result = 37 << 4;
            result |= type.hashCode();
            result <<= 4;
            result |= ObjectUtils.hashCode(value);
            return result;
        }

        @Override
        public String toString() {
            return String.format("Read-Only Position<%s>(%s)", Types.toString(type), value);
        }

    }

    private static class W<T> implements Position.Writable<T> {
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
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof W == false) {
                return false;
            }
            W<?> other = (W<?>) obj;
            return other.getType().equals(type);
        }

        @Override
        public int hashCode() {
            int result = 53 << 4;
            result |= type.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return String.format("Writable Position<%s>", Types.toString(type));
        }

    }

    private static class RW<T> implements Position.ReadWrite<T> {
        private final Type type;
        private T value;

        RW(Type type) {
            this.type = type;
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
            if (obj == this) {
                return true;
            }
            if (obj instanceof RW == false) {
                return false;
            }
            RW<?> other = (RW<?>) obj;
            return other.getType().equals(type) && ObjectUtils.equals(other.getValue(), value);
        }

        @Override
        public int hashCode() {
            int result = 43 << 4;
            result |= type.hashCode();
            result <<= 4;
            result |= ObjectUtils.hashCode(value);
            return result;
        }

        @Override
        public String toString() {
            return String.format("Read-Write Position<%s>(%s)", Types.toString(type), value);
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
     * @param value, not {@code null}
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
     * @param type not {@code null}
     * @param value
     * @return Position.Readable
     */
    public static <T> Position.Readable<T> readOnly(final TypeLiteral<T> type, final T value) {
        return readOnly(Validate.notNull(type, "type").value, value);
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
     * @param type not {@code null}
     * @return Position.ReadWrite
     */
    public static <T> Position.ReadWrite<T> readWrite(final TypeLiteral<T> type) {
        return readWrite(Validate.notNull(type, "type").value);
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
     * @param type not {@code null}
     * @param initialValue
     * @return Position.ReadWrite
     */
    public static <T> Position.ReadWrite<T> readWrite(final TypeLiteral<T> type, T initialValue) {
        return readWrite(Validate.notNull(type, "type").value, initialValue);
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
     * @param type not {@code null}
     * @return Position.Writable
     */
    public static <T> Position.Writable<T> writable(final TypeLiteral<T> type) {
        return writable(Validate.notNull(type, "type").value);
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
     * @see TherianContext#forwardTo(therian.Operation, UnaryProcedure)
     */
    public static <T> Procedure<T> writeValue(final Position.Writable<? super T> pos) {
        return new Procedure<T>() {

            @Override
            public void run(T value) {
                pos.setValue(value);
            }
        };
    }
}
