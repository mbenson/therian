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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.util.Types;

/**
 * Provides {@link Operator}-related utility methods and implements a sorted {@link Map} of {@link Operator} instance to
 * implemented {@link Operation} type.
 */
public class Operators extends AbstractMap<Operator<?>, Type> {

    private static final TypeVariable<?> OPERATION_VARIABLE = Operator.class.getTypeParameters()[0];

    /**
     * Get standard operators.
     *
     * @return Operator[]
     * @see Therian#standard()
     */
    public static Operator<?>[] standard() {
        return StandardOperators.STANDARD_OPERATORS.toArray(new Operator[0]);
    }

    /**
     * Validate an {@link Operator} implementation.
     *
     * @param operator
     * @param <OPERATOR>
     * @return {@code operator}
     * @throws OperatorDefinitionException on invalid operator
     */
    public static <OPERATOR extends Operator<?>> OPERATOR validateImplementation(OPERATOR operator) {
        for (TypeVariable<?> var : Validate.notNull(operator, "operator").getClass().getTypeParameters()) {
            if (Types.resolveAt(operator, var) == null) {
                throw new OperatorDefinitionException(operator, "Could not resolve %s against operator %s",
                    TypeUtils.toLongString(var), operator);
            }
        }
        return operator;
    }

    private final ListOrderedMap<Operator<?>, Type> contents = ListOrderedMap.listOrderedMap(new IdentityHashMap<>());

    public Operators(Collection<? extends Operator<?>> c) {
        super();
        addAll(c);
    }

    public boolean addAll(Collection<? extends Operator<?>> c) {
        Validate.notNull(c);
        boolean result = false;
        for (Operator<?> operator : c) {
            result = add(operator) || result;
        }
        return result;
    }

    public boolean add(Operator<?> operator) {
        Validate.notNull(operator);
        int pos = 0;

        final Type operationType = Types.resolveAt(operator, OPERATION_VARIABLE);
        for (Map.Entry<Operator<?>, Type> entry : contents.entrySet()) {
            if (compare(operationType, entry.getValue()) >= 0) {
                pos++;
                continue;
            }
            break;
        }
        contents.put(pos, operator, operationType);
        return true;
    }

    @Override
    public int size() {
        return contents.size();
    }

    private int compare(Type t1, Type t2) {
        if (t1 == t2 || TypeUtils.equals(t1, t2)) {
            return 0;
        }
        if (TypeUtils.isAssignable(t1, t2)) {
            return -1;
        }
        if (TypeUtils.isAssignable(t2, t1)) {
            return 1;
        }
        final Class<?> raw1 = raw(t1);
        Validate.validState(raw1 != null, "Cannot get raw type for %s", t1);
        final Class<?> raw2 = raw(t2);
        Validate.validState(raw2 != null, "Cannot get raw type for %s", t2);

        if (raw1.equals(raw2)) {
            if (raw1.getTypeParameters().length == 0) {
                return 0;
            }
            if (t1 instanceof ParameterizedType) {
                if (t2 instanceof ParameterizedType) {
                    Type[] args1 = ((ParameterizedType) t1).getActualTypeArguments();
                    Type[] args2 = ((ParameterizedType) t2).getActualTypeArguments();
                    for (int i = 0; i < args1.length; i++) {
                        final int recurse = compare(args1[i], args2[i]);
                        if (recurse != 0) {
                            return recurse;
                        }
                    }
                } else {
                    return -1;
                }
            } else if (t2 instanceof ParameterizedType) {
                return 1;
            }
            return 0;
        }
        final int steps = StringUtils.countMatches(raw1.getName(), ".") - StringUtils.countMatches(raw2.getName(), ".");
        if (steps == 0) {
            return raw1.getName().compareTo(raw2.getName());
        }
        return steps;
    }

    private Class<?> raw(Type type) {
        if (type instanceof WildcardType) {
            final Type upper = TypeUtils.getImplicitUpperBounds((WildcardType) type)[0];
            return upper instanceof Class<?> ? (Class<?>) upper : raw(upper);
        }
        return TypeUtils.getRawType(type, null);
    }

    @Override
    public Set<java.util.Map.Entry<Operator<?>, Type>> entrySet() {
        return contents.entrySet();
    }
}
