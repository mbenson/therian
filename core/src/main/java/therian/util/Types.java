package therian.util;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeUtils;

public class Types {

    public static Type refine(Type type, Type parentType) {
        if (type instanceof TypeVariable) {
            return TypeUtils.normalizeUpperBounds(((TypeVariable<?>) type).getBounds())[0];
        }
        return type;
    }

    /**
     * Get the argument to a given type variable, unrolling variable-to-variable assignments among a class hierarchy
     * @param typeArguments as from {@link TypeUtils#getTypeArguments(Type, Class)}
     * @param typeVariable
     * @return assigned Type
     */
    public static Type get(Map<TypeVariable<?>, Type> typeArguments, TypeVariable<?> typeVariable) {
        Type result = typeArguments.get(typeVariable);
        while (result instanceof TypeVariable<?>) {
            result = typeArguments.get(result);
        }
        return result;
    }
}
