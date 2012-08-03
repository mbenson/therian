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
package therian;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Comparator;
import java.util.Map;

import org.apache.commons.functor.UnaryPredicate;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import therian.operator.ConvertingCopier;
import therian.operator.DefaultImmutableChecker;
import therian.operator.ELCoercionConverter;

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
        new DefaultImmutableChecker()
    };
    //@formatter:on

    private static final Comparator<Operator<?>> COMPARATOR = new Comparator<Operator<?>>() {

        public int compare(Operator<?> o1, Operator<?> o2) {
            final Type opType1 =
                TypeUtils.getTypeArguments(o1.getClass(), Operator.class).get(Operator.class.getTypeParameters()[0]);
            final Type opType2 =
                TypeUtils.getTypeArguments(o2.getClass(), Operator.class).get(Operator.class.getTypeParameters()[0]);

            if (ObjectUtils.equals(opType1, opType2)) {
                return 0;
            }
            if (TypeUtils.isAssignable(opType1, opType2)) {
                return -1;
            }
            if (TypeUtils.isAssignable(opType2, opType1)) {
                return 1;
            }
            final Class<?> raw1 = TypeUtils.getRawType(opType1, o1.getClass());
            final Class<?> raw2 = TypeUtils.getRawType(opType2, o2.getClass());
            if (ObjectUtils.equals(raw1, raw2)) {
                return compareTypes(ImmutablePair.of(opType1, o1.getClass()), ImmutablePair.of(opType2, o2.getClass()));
            }
            return opType1.toString().compareTo(opType2.toString());
        }

        /**
         * Compare types
         * 
         * @param p1
         *            first pair of type, assigning type
         * @param p2
         *            second pair of type, assigning type
         * @return int
         */
        private int compareTypes(ImmutablePair<? extends Type, ? extends Type> p1,
            ImmutablePair<? extends Type, ? extends Type> p2) {
            if (ObjectUtils.equals(p1.left, p2.left)) {
                return 0;
            }
            if (TypeUtils.isAssignable(p1.left, p2.left)) {
                return -1;
            }
            if (TypeUtils.isAssignable(p2.left, p1.left)) {
                return 1;
            }
            final Class<?> raw1 = TypeUtils.getRawType(p1.left, p1.right);
            final Class<?> raw2 = TypeUtils.getRawType(p2.left, p2.right);
            if (ObjectUtils.equals(raw1, raw2)) {
                if (raw1.getTypeParameters().length == 0) {
                    return 0;
                }
                final Map<TypeVariable<?>, Type> typeArgs1 = TypeUtils.getTypeArguments(p1.left, raw1);
                final Map<TypeVariable<?>, Type> typeArgs2 = TypeUtils.getTypeArguments(p2.left, raw2);
                for (TypeVariable<?> var : raw1.getTypeParameters()) {
                    final int recurse =
                        compareTypes(ImmutablePair.of(typeArgs1.get(var), p1.right),
                            ImmutablePair.of(typeArgs2.get(var), p2.right));
                    if (recurse != 0) {
                        return recurse;
                    }
                }
                return 0;
            }
            return p1.left.toString().compareTo(p2.left.toString());
        }
    };

    /**
     * Get standard operators.
     * 
     * @return Operator[]
     */
    public static Operator<?>[] standard() {
        return STANDARD_OPERATORS;
    }

    /**
     * Get a {@link UnaryPredicate} to match {@link Operator}s that support {@code Operation}.
     * 
     * @param operation
     * @return {@link UnaryPredicate}
     */
    public static UnaryPredicate<Operator<?>> supporting(final Operation<?> operation) {
        return new UnaryPredicate<Operator<?>>() {
            public boolean test(Operator<?> operator) {
                final Map<TypeVariable<?>, Type> typeArguments =
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
     * @param <OPERATOR>
     * @return {@code operator}
     * @throws OperatorDefinitionException
     *             on invalid operator
     */
    public static <OPERATOR extends Operator<?>> OPERATOR validateImplementation(OPERATOR operator) {
        if (operator.getClass().getTypeParameters().length > 0) {
            throw new OperatorDefinitionException(operator, TYPE_PARAMS_DETECTED);
        }
        return operator;
    }

    /**
     * Get a comparator that compares {@link Operator}s by {@link Operation} type/type parameter assignability.
     * 
     * @return a Comparator that does not handle {@code null} values
     */
    public static Comparator<Operator<?>> comparator() {
        return COMPARATOR;
    }
}
