package therian.position;

import java.util.Objects;

import org.apache.commons.lang3.reflect.TypeUtils;

/**
 * Base {@link Position} implementation,
 * 
 * @param <T>
 */
public abstract class AbstractPosition<T> implements Position<T> {

    /**
     * Base readable {@link Position}.
     *
     * @param <T>
     */
    public static abstract class Readable<T> extends AbstractPosition<T> implements Position.Readable<T> {

        /**
         * {@inheritDoc} Based on type and value.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!getClass().isInstance(obj)) {
                return false;
            }
            final Position.Readable<?> other = (Position.Readable<?>) obj;
            return TypeUtils.equals(getType(), other.getType()) && Objects.equals(other.getValue(), getValue());
        }

        /**
         * {@inheritDoc} Based on type and value.
         */
        @Override
        public int hashCode() {
            return Objects.hash(getType(), getValue());
        }
    }

    /**
     * Base writable {@link Position}.
     *
     * @param <T>
     */
    public static abstract class Writable<T> extends AbstractPosition<T> implements Position.Writable<T> {

        /**
         * {@inheritDoc} Based on type only.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!getClass().isInstance(obj)) {
                return false;
            }
            final Position.Writable<?> other = (Position.Writable<?>) obj;
            return TypeUtils.equals(getType(), other.getType());
        }

        /**
         * {@inheritDoc} Based on type only.
         */
        @Override
        public int hashCode() {
            return Objects.hash(getType());
        }
    }
}
