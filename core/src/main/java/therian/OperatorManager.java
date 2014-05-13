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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.functor.Predicate;
import org.apache.commons.functor.core.collection.FilteredIterator;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator.DependsOn;
import therian.util.Types;

/**
 * Manages {@link Operator}s for a given {@link Therian} instance in an attempt to improve efficiency.
 * 
 * <ul>
 * <li>Sorts operators using {@link Operators#comparator()}</li>
 * <li>Caches expected operation type per {@link Operator} instance</li>
 * <li>breaks operators into subgroups by raw operation type</li>
 * </ul>
 */
class OperatorManager {
    @SuppressWarnings("rawtypes")
    private static class OperatorInfo implements Comparable<OperatorInfo> {
        private static final Comparator<Operator<?>> OPERATOR_COMPARATOR = Operators.comparator();

        final Operator operator;
        final Type targetType;
        final Class<?> rawTargetType;

        OperatorInfo(Operator operator) {
            this.operator = operator;
            targetType =
                TypeUtils.unrollVariables(TypeUtils.getTypeArguments(operator.getClass(), Operator.class),
                    Operator.class.getTypeParameters()[0]);
            rawTargetType = getRawType(targetType);
        }

        private static Class<?> getRawType(Type targetType) {
            if (targetType instanceof Class<?>) {
                return (Class<?>) targetType;
            }
            if (targetType instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) targetType).getRawType();
            }
            if (targetType instanceof WildcardType) {
                return getRawType(TypeUtils.getImplicitUpperBounds((WildcardType) targetType)[0]);
            }
            throw new IllegalArgumentException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(OperatorInfo o) {
            return OPERATOR_COMPARATOR.compare(this.operator, o.operator);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return operator.toString();
        }
    }

    class SupportChecker {

        @SuppressWarnings("rawtypes")
        private class Filter implements Predicate<OperatorInfo> {
            final Operation operation;

            Filter(Operation operation) {
                this.operation = operation;
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean test(OperatorInfo info) {
                if (matches(operation, info)) {
                    return info.operator.supports(context, operation);
                }
                return false;
            }

            private boolean matches(Operation operation, OperatorInfo operatorInfo) {
                if (!TypeUtils.isInstance(operation, operatorInfo.targetType)) {
                    return false;
                }

                for (Class<?> c : ClassUtils.hierarchy(operatorInfo.rawTargetType)) {
                    if (c.equals(Operation.class)) {
                        break;
                    }
                    final Map<TypeVariable<?>, Type> typeArguments =
                        TypeUtils.getTypeArguments(operatorInfo.targetType, c);

                    if (typeArguments != null) {
                        for (TypeVariable<?> var : c.getTypeParameters()) {
                            Type type = Types.resolveAt(operation, var);
                            if (type == null) {
                                continue;
                            }
                            if (type instanceof Class<?> && ((Class<?>) type).isPrimitive()) {
                                type = ClassUtils.primitiveToWrapper((Class<?>) type);
                            }
                            if (!TypeUtils.isAssignable(type, TypeUtils.unrollVariables(typeArguments, var))) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            }

        }

        private final TherianContext context;

        SupportChecker(TherianContext context) {
            this.context = context;
        }

        Iterable<Operator<?>> operatorsSupporting(final Operation<?> operation) {
            final Filter filter = new Filter(operation);
            final Iterator<Class<?>> hierarchy = ClassUtils.hierarchy(operation.getClass()).iterator();

            return new Iterable<Operator<?>>() {

                @Override
                public Iterator<Operator<?>> iterator() {
                    return new Iterator<Operator<?>>() {
                        Iterator<OperatorInfo> currentInfo;

                        @Override
                        public boolean hasNext() {
                            while (currentInfo == null || !currentInfo.hasNext()) {
                                if (hierarchy.hasNext()) {
                                    final Class<?> c = hierarchy.next();
                                    if (subgroups.containsKey(c)) {
                                        currentInfo = FilteredIterator.filter(subgroups.get(c).iterator(), filter);
                                    }
                                    continue;
                                }
                                return false;
                            }
                            return true;
                        }

                        @Override
                        public Operator<?> next() {
                            if (hasNext()) {
                                return currentInfo.next().operator;
                            }
                            throw new NoSuchElementException();
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            };
        }
    }

    private final List<OperatorInfo> operatorInfos;
    private final Map<Class<?>, Iterable<OperatorInfo>> subgroups;

    OperatorManager(Set<Operator<?>> operators) {
        validate(operators);
        operatorInfos = Collections.unmodifiableList(buildOperatorInfos(operators));
        subgroups = Collections.unmodifiableMap(buildOperatorInfoSubgroups(operatorInfos));
    }

    private static void validate(Set<Operator<?>> operators) {
        final Set<Class<?>> operatorsPresent = new HashSet<Class<?>>();
        final Set<Class<?>> operatorsNeeded = new HashSet<Class<?>>();

        for (Operator<?> operator : operators) {
            Operators.validateImplementation(operator);

            final Class<?> opType = operator.getClass();
            operatorsPresent.add(opType);

            for (Class<?> c : ClassUtils.hierarchy(opType)) {
                final DependsOn dependsOn = c.getAnnotation(DependsOn.class);
                if (dependsOn != null) {
                    Collections.addAll(operatorsNeeded, dependsOn.value());
                }
            }
        }
        operatorsNeeded.removeAll(operatorsPresent);
        Validate.isTrue(operatorsNeeded.isEmpty(), "Missing required operators: %s", operatorsNeeded);
    }

    private static List<OperatorInfo> buildOperatorInfos(Set<Operator<?>> operators) {
        final List<OperatorInfo> result = new ArrayList<OperatorInfo>();
        for (Operator<?> operator : operators) {
            final OperatorInfo info = new OperatorInfo(operator);
            result.add(info);
        }
        Collections.sort(result);
        return result;
    }

    private static Map<Class<?>, Iterable<OperatorInfo>> buildOperatorInfoSubgroups(List<OperatorInfo> operatorInfos) {
        final Map<Class<?>, Iterable<OperatorInfo>> result = new HashMap<Class<?>, Iterable<OperatorInfo>>();

        Class<?> opType = null;
        int mark = -1;

        for (int i = 0, sz = operatorInfos.size(); i < sz; i++) {
            final OperatorInfo info = operatorInfos.get(i);
            if (info.rawTargetType.equals(opType)) {
                continue;
            }
            if (opType != null) {
                result.put(opType, operatorInfos.subList(mark, i));
            }
            opType = info.rawTargetType;
            mark = i;
        }
        result.put(opType, operatorInfos.subList(mark, operatorInfos.size()));
        return result;
    }

}
