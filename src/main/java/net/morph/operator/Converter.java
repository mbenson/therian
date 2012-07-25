package net.morph.operator;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import net.morph.Operator;
import net.morph.operation.Convert;

import org.apache.commons.lang3.reflect.TypeUtils;

/**
 * {@link Convert} {@link Operator} superclass.
 * 
 * @param <SOURCE>
 * @param <DEST>
 */
public abstract class Converter<SOURCE, DEST> implements Operator<Convert<? extends SOURCE, ? super DEST>> {
    private static final TypeVariable<?>[] TYPE_PARAMS = Converter.class.getTypeParameters();

    public boolean supports(Convert<? extends SOURCE, ? super DEST> convert) {
        final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(getClass(), Converter.class);
        return TypeUtils.isInstance(convert.getSourcePosition().getValue(), typeArguments.get(TYPE_PARAMS[0]))
            && TypeUtils.isAssignable(typeArguments.get(TYPE_PARAMS[1]), convert.getTargetPosition().getType());
    }
}
