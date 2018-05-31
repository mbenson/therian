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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.el.ELContextListener;
import javax.el.ELResolver;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import therian.Operator.DependsOn;

/**
 * Therian module.
 */
public class TherianModule {
    protected static Operator<?>[] withDependencies(Operator<?>... operators) {
        if (operators == null) {
            return new Operator[0];
        }
        Validate.noNullElements(operators, "null operator at index %s");

        @SuppressWarnings("rawtypes")
        final Set<Class<? extends Operator>> operatorTypesPresent = new HashSet<>();
        @SuppressWarnings("rawtypes")
        final Set<Class<? extends Operator>> operatorTypesNeeded = new HashSet<>();

        @SuppressWarnings("rawtypes")
        class DependencyManager {

            void handle(Operator<?> operator) {
                final Class<? extends Operator> type = operator.getClass();
                operatorTypesPresent.add(type);
                operatorTypesNeeded.remove(type);

                Class<?> c = type;
                while (c != null) {
                    handle(c.getAnnotation(DependsOn.class));
                    c = c.getSuperclass();
                }
            }

            void handle(DependsOn deps) {
                if (deps != null) {
                    for (Class<? extends Operator> type : deps.value()) {
                        handle(type);
                    }
                }
            }

            void handle(Class<? extends Operator> type) {
                if (!operatorTypesPresent.contains(type)) {
                    operatorTypesNeeded.add(type);
                }
                handle(type.getAnnotation(DependsOn.class));
            }
        }

        final DependencyManager dependencyManager = new DependencyManager();
        for (Operator<?> op : operators) {
            dependencyManager.handle(op);
        }
        if (operatorTypesNeeded.isEmpty()) {
            return operators;
        }
        final Operator<?>[] deps = new Operator[operatorTypesNeeded.size()];
        int index = 0;
        for (@SuppressWarnings("rawtypes")
        Class<? extends Operator> dep : operatorTypesNeeded) {
            try {
                deps[index++] = dep.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return ArrayUtils.addAll(operators, deps);
    }

    public static TherianModule create() {
        return new TherianModule();
    }

    public static TherianModule expandingDependencies(final TherianModule module) {
        Validate.notNull(module, "module");

        final Operator<?>[] expandedOperators = withDependencies(module.getOperators());
        if (Arrays.equals(expandedOperators, module.getOperators())) {
            return module;
        }
        return TherianModule.create().withELContextListeners(module.getElContextListeners())
            .withELResolvers(module.getElResolvers()).withOperators(expandedOperators);
    }

    private static <T> T[] toArray(Iterable<T> iterable, Class<?> componentType) {
        final Collection<T> coll;
        if (iterable instanceof Collection<?>) {
            coll = (Collection<T>) iterable;
        } else {
            coll = IteratorUtils.toList(iterable.iterator());
        }
        @SuppressWarnings("unchecked")
        final T[] result = (T[]) Array.newInstance(componentType, coll.size());
        return coll.toArray(result);
    }

    private ELResolver[] elResolvers;
    private ELContextListener[] elContextListeners;
    private Operator<?>[] operators;

    protected TherianModule() {
    }

    public synchronized ELResolver[] getElResolvers() {
        if (elResolvers == null) {
            elResolvers = new ELResolver[0];
        }
        return elResolvers;
    }

    public ELContextListener[] getElContextListeners() {
        if (elContextListeners == null) {
            elContextListeners = new ELContextListener[0];
        }
        return elContextListeners;
    }

    public Operator<?>[] getOperators() {
        if (operators == null) {
            operators = new Operator[0];
        }
        return operators;
    }

    public TherianModule withELResolvers(ELResolver... elResolvers) {
        this.elResolvers = elResolvers;
        return this;
    }

    public TherianModule withELResolvers(Iterable<ELResolver> elResolvers) {
        return withELResolvers(toArray(elResolvers, ELResolver.class));
    }

    public TherianModule withELContextListeners(ELContextListener... elContextListeners) {
        this.elContextListeners = elContextListeners;
        return this;
    }

    public TherianModule withELContextListeners(Iterable<ELContextListener> elContextListeners) {
        return withELContextListeners(toArray(elContextListeners, ELContextListener.class));
    }

    public TherianModule withOperators(Operator<?>... operators) {
        this.operators = operators;
        return this;
    }

    public TherianModule withOperators(Iterable<Operator<?>> operators) {
        return withOperators(toArray(operators, Operator.class));
    }
}
