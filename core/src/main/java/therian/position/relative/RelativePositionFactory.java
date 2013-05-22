package therian.position.relative;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;

import therian.TherianContext;
import therian.position.Position;

/**
 * Describes an entity capable of producing {@link RelativePosition}s.
 *
 * @param <PARENT>
 * @param <TYPE>
 */
public abstract class RelativePositionFactory<PARENT, TYPE> {
    protected abstract class RelativePositionImpl<P extends PARENT, E> implements RelativePosition<P, TYPE> {
        protected final Position.Readable<P> parentPosition;
        private final E name;

        protected RelativePositionImpl(Position.Readable<P> parentPosition, E name) {
            this.parentPosition = Validate.notNull(parentPosition);
            this.name = name;
        }

        @Override
        public therian.position.Position.Readable<? extends P> getParentPosition() {
            return parentPosition;
        }

        public TYPE getValue() {
            final TherianContext context = TherianContext.getInstance();
            final Object value = context.getELResolver().getValue(context, parentPosition.getValue(), name);
            Validate.validState(context.isPropertyResolved(), "could not get value %s from %s", name, parentPosition);
            @SuppressWarnings("unchecked")
            final TYPE result = (TYPE) value;
            return result;
        }

        public void setValue(TYPE value) {
            final TherianContext context = TherianContext.getInstance();
            context.getELResolver().setValue(context, parentPosition.getValue(), name, value);
            Validate.validState(context.isPropertyResolved(), "could not set value %s onto %s from %s", value, name,
                parentPosition);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof RelativePositionFactory.RelativePositionImpl == false) {
                return false;
            }
            @SuppressWarnings("unchecked")
            final RelativePositionImpl<?, ?> other = (RelativePositionImpl<?, ?>) obj;
            return ObjectUtils.equals(getFactory(), other.getFactory())
                && ObjectUtils.equals(parentPosition, other.parentPosition);
        }

        @Override
        public int hashCode() {
            int result = 97;
            result <<= 8;
            result |= getFactory().hashCode();
            result <<= 8;
            result |= parentPosition.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return String.format("Relative Position: %s of %s", getFactory(), parentPosition);
        }

        private RelativePositionFactory<PARENT, TYPE> getFactory() {
            return RelativePositionFactory.this;
        }
    }

    /**
     * Obtain the {@link RelativePosition}.
     *
     * @param parentPosition
     * @return {@link RelativePosition}
     */
    public abstract <P extends PARENT> RelativePosition<P, TYPE> of(Position.Readable<P> parentPosition);

}
