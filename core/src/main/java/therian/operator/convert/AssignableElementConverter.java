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
package therian.operator.convert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.reflect.Typed;

import therian.TherianContext;
import therian.operation.Convert;

/**
 * Abstract {@link Converter} of objects with elements that are assignable from source to target.
 *
 * @param <SOURCE>
 * @param <TARGET>
 */
public abstract class AssignableElementConverter<SOURCE, TARGET> extends Converter<SOURCE, TARGET> {
    private final TypeVariable<? extends Class<?>> sourceElementType;
    private final TypeVariable<? extends Class<?>> targetElementType;

    /**
     * Create an {@link AssignableElementConverter}.
     *
     * @param sourceElementType
     * @param targetElementType
     */
    protected AssignableElementConverter(TypeVariable<? extends Class<?>> sourceElementType,
        TypeVariable<? extends Class<?>> targetElementType) {
        super();
        this.sourceElementType = sourceElementType;
        this.targetElementType = targetElementType;
    }

    @Override
    public boolean supports(TherianContext context, Convert<? extends SOURCE, ? super TARGET> convert) {
        if (!super.supports(context, convert)) {
            return false;
        }
        final Type targetComponentType = targetComponentType(convert.getTargetPosition());
        final Type sourceComponentType = sourceComponentType(convert.getSourcePosition());

        return sourceComponentType != null && TypeUtils.isAssignable(sourceComponentType, targetComponentType);
    }

    protected Type sourceComponentType(Typed<?> item) {
        final Type t = item.getType();
        final Class<?> varOwner = sourceElementType.getGenericDeclaration();

        final Map<TypeVariable<?>, Type> args = TypeUtils.getTypeArguments(t, varOwner);
        return args == null ? null : ObjectUtils.defaultIfNull(TypeUtils.unrollVariables(args, sourceElementType),
            Object.class);
    }

    protected Type targetComponentType(Typed<?> item) {
        final Type t = item.getType();
        final Class<?> varOwner = targetElementType.getGenericDeclaration();

        if (t instanceof Class<?>) {
            // raw
            return Object.class;
        }

        if (TypeUtils.isAssignable(t, varOwner)) {
            return TypeUtils.unrollVariables(TypeUtils.getTypeArguments(t, varOwner), targetElementType);
        }
        if (t instanceof ParameterizedType) {
            final Map<TypeVariable<?>, Type> args = TypeUtils.determineTypeArguments(varOwner, (ParameterizedType) t);
            return args.get(targetElementType);
        }
        return null;
    }
}
