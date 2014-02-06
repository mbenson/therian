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
package therian.module;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ClassUtils.Interfaces;
import org.apache.commons.lang3.reflect.ConstructorUtils;

import therian.Operator;
import therian.TherianModule;

/**
 * {@link TherianModule} that includes an instance of each instantiable {@link Operator} type defined as an inner class
 * of its inheritance hierarchy.
 */
public abstract class SelfContainedTherianModule extends TherianModule {

    private final Interfaces interfacesPolicy;

    /**
     * Create a {@link SelfContainedTherianModule} including interfaces.
     *
     * @see SelfContainedTherianModule#SelfContainedTherianModule(Interfaces)
     */
    protected SelfContainedTherianModule() {
        this(Interfaces.INCLUDE);
    }

    /**
     * Create a {@link SelfContainedTherianModule}.
     *
     * @param interfacesPolicy to use when discovering {@link Operator} inner classes
     */
    protected SelfContainedTherianModule(Interfaces interfacesPolicy) {
        this.interfacesPolicy = interfacesPolicy;
    }

    @Override
    public Operator<?>[] getOperators() {
        final Operator<?>[] explicit = super.getOperators();
        final Operator<?>[] selfContained = getSelfContainedOperators();
        return withDependencies(ArrayUtils.addAll(explicit, selfContained));
    }

    private Operator<?>[] getSelfContainedOperators() {
        final List<Operator<?>> result = new ArrayList<Operator<?>>();

        for (Class<?> c : ClassUtils.hierarchy(getClass(), interfacesPolicy)) {
            for (Class<?> inner : c.getDeclaredClasses()) {
                if (Operator.class.isAssignableFrom(inner)) {
                    final Operator<?> operator = newInstance(inner.asSubclass(Operator.class));
                    if (operator != null) {
                        result.add(operator);
                    }
                }
            }
        }
        return result.toArray(new Operator[result.size()]);
    }

    @SuppressWarnings("rawtypes")
    private <O extends Operator> O newInstance(Class<O> c) {
        if (c.isInterface()) {
            return null;
        }
        if (Modifier.isAbstract(c.getModifiers())) {
            return null;
        }
        final Class<?>[] paramTypes;
        final Object[] args;
        if (Modifier.isStatic(c.getModifiers())) {
            paramTypes = ArrayUtils.EMPTY_CLASS_ARRAY;
            args = ArrayUtils.EMPTY_OBJECT_ARRAY;
        } else {
            paramTypes = new Class[] { getClass() };
            args = new Object[] { this };
        }
        final Constructor<O> cs = ConstructorUtils.getMatchingAccessibleConstructor(c, paramTypes);
        if (cs == null) {
            return null;
        }
        try {
            return cs.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
