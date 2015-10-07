package therian.operator;

import java.util.Objects;

import therian.Operation;
import therian.Operator;

public abstract class OperatorBase<T extends Operation<?>> implements Operator<T> {
    @Override
    public boolean equals(Object obj) {
        return obj == this || getClass().isInstance(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
