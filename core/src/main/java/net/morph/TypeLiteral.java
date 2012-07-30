package net.morph;

import java.lang.reflect.Type;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

/**
 * Same old "Type literal."
 */
public abstract class TypeLiteral<T> {
    public final Type value;

    protected TypeLiteral() {
        this.value =
            ObjectUtils
                .defaultIfNull(
                    TypeUtils.getTypeArguments(getClass(), TypeLiteral.class).get(
                        TypeLiteral.class.getTypeParameters()[0]), Object.class);
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof TypeLiteral == false) {
            return false;
        }
        TypeLiteral<?> other = (TypeLiteral<?>) obj;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return (37 << 4) | value.hashCode();
    }
}
