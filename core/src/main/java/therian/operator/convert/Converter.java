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

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import therian.Operator;
import therian.TherianContext;
import therian.operation.Convert;
import therian.util.Types;

/**
 * {@link Convert} {@link Operator} superclass.
 *
 * Note the assignability constraints:
 * <ul>
 * <li>SOURCE assignable from source type</li>
 * <li>TARGET assignable to target type</li>
 * </ul>
 *
 * For example, if you wanted to convert a {@link String} to a {@link Number}, then a {@link Converter} of
 * {@link CharSequence} to {@link Integer} would satisfy. The inverse is not necessarily true: a {@link Converter}
 * declared to convert {@link String} to {@link Number} may not be able to handle the source value of, or produce a
 * target value compatible with, a conversion requested from {@link CharSequence} to {@link Integer}. Thus it is best to
 * define your domain APIs as widely as possible, and for your {@link Converter} implementations, parameterize SOURCE as
 * widely as possible and TARGET as narrowly as possible.
 *
 * @param <SOURCE>
 * @param <TARGET>
 */
public abstract class Converter<SOURCE, TARGET> implements Operator<Convert<? extends SOURCE, ? super TARGET>> {

    /**
     * Accommodates the situation where a {@link Converter} applies to multiple target types that share no common
     * descendant as in the case of e.g. array types.
     */
    public static abstract class WithDynamicTarget<SOURCE> implements Operator<Convert<? extends SOURCE, ?>> {
        private static final TypeVariable<?>[] TYPE_PARAMS = Converter.WithDynamicTarget.class.getTypeParameters();

        /**
         * {@link Logger} instance.
         */
        protected final Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public boolean supports(TherianContext context, Convert<? extends SOURCE, ?> convert) {
            return TypeUtils.isInstance(convert.getSourcePosition().getValue(), Types.unrollVariables(
                TypeUtils.getTypeArguments(getClass(), Converter.WithDynamicTarget.class), TYPE_PARAMS[0]));
        }
    }

    /**
     * {@link Logger} instance.
     */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final Type sourceBound;
    private final Type targetBound;

    {
        final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(getClass(), Converter.class);
        sourceBound = Types.unrollVariables(typeArguments, Converter.class.getTypeParameters()[0]);
        targetBound = Types.unrollVariables(typeArguments, Converter.class.getTypeParameters()[1]);
    }

    /**
     * Get the (upper) source bound.
     *
     * @return Type
     */
    public Type getSourceBound() {
        return sourceBound;
    }

    /**
     * Get the (lower) target bound.
     *
     * @return Type
     */
    public Type getTargetBound() {
        return targetBound;
    }

    @Override
    public boolean supports(TherianContext context, Convert<? extends SOURCE, ? super TARGET> convert) {
        return TypeUtils.isInstance(convert.getSourcePosition().getValue(), sourceBound)
            && TypeUtils.isAssignable(targetBound, convert.getTargetPosition().getType());
    }

}
