package net.morph.operator;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import net.morph.MorphContext;
import net.morph.Operator;
import net.morph.operation.Copy;
import net.morph.operation.ImmutableCheck;

import org.apache.commons.lang3.reflect.TypeUtils;

/**
 * {@link Copy} {@link Operator} superclass.
 * 
 * @param <SOURCE>
 * @param <DEST>
 */
public abstract class Copier<SOURCE, DEST> implements Operator<Copy<? extends SOURCE, ? extends DEST>> {
    private static final TypeVariable<?>[] TYPE_PARAMS = Copier.class.getTypeParameters();

    public boolean supports(Copy<? extends SOURCE, ? extends DEST> copy) {
        // cannot copy to immutable types
        if (MorphContext.getRequiredInstance().perform(ImmutableCheck.of(copy.getTargetPosition().getValue()))
            .booleanValue()) {
            return false;
        }
        final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(getClass(), Copier.class);
        return TypeUtils.isInstance(copy.getSourcePosition().getValue(), typeArguments.get(TYPE_PARAMS[0]))
            && TypeUtils.isAssignable(copy.getTargetPosition().getType(), typeArguments.get(TYPE_PARAMS[1]));
    }
}
