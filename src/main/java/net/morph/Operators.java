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
package net.morph;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import net.morph.operator.ConvertingCopier;
import net.morph.operator.ELCoercionConverter;

import org.apache.commons.functor.UnaryPredicate;
import org.apache.commons.lang3.reflect.TypeUtils;

/**
 * Utility methods for Operators.
 */
public class Operators {
    private static final String TYPE_PARAMS_DETECTED = "Should not instantiate parameterized Operator types";

    //@formatter:off
    private static final Operator<?>[] STANDARD_OPERATORS = {
        /*
         * TODO add
         */
        new ELCoercionConverter(),
        new ConvertingCopier(),
    };
    //@formatter:on

    /**
     * Get standard operators.
     * 
     * @return Operator[]
     */
    public static Operator<?>[] standard() {
        return STANDARD_OPERATORS;
    }

    /**
     * Get a {@link UnaryPredicate} to match {@link Operator}s that support
     * {@code Operation}.
     * 
     * @param operation
     * @return {@link UnaryPredicate}
     */
    public static UnaryPredicate<Operator<?>> supporting(final Operation<?> operation) {
        return new UnaryPredicate<Operator<?>>() {
            public boolean test(Operator<?> operator) {
                Map<TypeVariable<?>, Type> typeArguments =
                    TypeUtils.getTypeArguments(operator.getClass(), Operator.class);
                if (TypeUtils.isInstance(operation, typeArguments.get(Operator.class.getTypeParameters()[0]))) {
                    @SuppressWarnings({ "rawtypes", "unchecked" })
                    final boolean result = ((Operator) operator).supports(operation);
                    return result;
                }
                return false;
            }
        };
    }

    /**
     * Validate an Operator implementation.
     * 
     * @param operator
     * @param <RESULT>
     * @param <OPERATOR>
     * @return {@code operator}
     * @throws OperatorDefinitionException
     *             on invalid operator
     */
    public static <RESULT, OPERATOR extends Operator<Operation<RESULT>>> OPERATOR validateImplementation(
        OPERATOR operator) {
        if (operator.getClass().getTypeParameters().length > 0) {
            throw new OperatorDefinitionException(TYPE_PARAMS_DETECTED);
        }
        return operator;
    }
}
