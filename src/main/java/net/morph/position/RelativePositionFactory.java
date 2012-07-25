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
package net.morph.position;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.morph.position.RelativePosition.Implements;
import net.morph.position.RelativePosition.Mixin;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

/**
 * Relative Position factory.
 */
public abstract class RelativePositionFactory<TYPE, PARENT_TYPE, POSITION_TYPE extends Position<TYPE>> {
    private class RelativePositionInvocationHandler implements InvocationHandler {
        final PARENT_TYPE parent;
        final Map<Class<? extends Position<?>>, Mixin<PARENT_TYPE, TYPE>> mixins;

        protected RelativePositionInvocationHandler(PARENT_TYPE parent,
            Map<Class<? extends Position<?>>, Mixin<PARENT_TYPE, TYPE>> mixins) {
            super();
            this.parent = parent;
            this.mixins = mixins;
        }

        /**
         * {@inheritDoc}
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Mixin<PARENT_TYPE, TYPE> mixin = mixins.get(method.getDeclaringClass());
            if (mixin != null) {
                return MethodUtils.invokeMethod(mixin, method.getName(), ArrayUtils.add(args, 0, parent));
            }
            if (method.equals(Object.class.getMethod("equals", Object.class))) {
                return args[0].getClass().equals(proxy.getClass()) && Proxy.getInvocationHandler(args[0]).equals(this);
            }
            if (method.equals(Object.class.getMethod("hashCode"))) {
                return System.identityHashCode(proxy);
            }
            if (method.equals(Object.class.getMethod("toString"))) {
                return String.format("Relative Position: %s of %s", RelativePositionFactory.this.getClass()
                    .getSimpleName(), parent);
            }
            throw new UnsupportedOperationException(String.format("%s %s(%s)", method.getReturnType(),
                method.getName(), Arrays.toString(args)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof RelativePositionFactory<?, ?, ?>.RelativePositionInvocationHandler == false) {
                return false;
            }
            RelativePositionFactory<?, ?, ?>.RelativePositionInvocationHandler other =
                (RelativePositionFactory<?, ?, ?>.RelativePositionInvocationHandler) obj;
            return ObjectUtils.equals(parent, other.parent) && ObjectUtils.equals(mixins, other.mixins);
        }
    }

    private final Map<Class<? extends Position<?>>, Mixin<PARENT_TYPE, TYPE>> mixins;

    /**
     * Create a new RelativePositionFactory instance.
     * 
     * @param mixins
     */
    protected RelativePositionFactory(Mixin<PARENT_TYPE, TYPE>... mixins) {
        super();
        Map<Class<? extends Position<?>>, Mixin<PARENT_TYPE, TYPE>> m =
            new HashMap<Class<? extends Position<?>>, RelativePosition.Mixin<PARENT_TYPE, TYPE>>();
        for (Mixin<PARENT_TYPE, TYPE> mixin : mixins) {
            for (Class<? extends Position<?>> positionType : getImplementedTypes(mixin.getClass())) {
                if (m.containsKey(positionType)) {
                    throw new IllegalArgumentException(String.format("%s implemented by > 1 of mixins %s",
                        positionType, mixins));
                }
                m.put(positionType, mixin);
            }
        }
        this.mixins = m;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Class<? extends Position<?>>[] getImplementedTypes(Class<? extends Mixin> mixinType) {
        HashSet<Class<? extends Position>> set = new HashSet<Class<? extends Position>>();
        for (Class<?> iface : mixinType.getInterfaces()) {
            Implements annotation = iface.getAnnotation(Implements.class);
            if (annotation != null) {
                Collections.addAll(set, annotation.value());
            }
        }
        return set.toArray(new Class[set.size()]);
    }

    /**
     * Get a Position relative to parent.
     * 
     * @param parent
     * @return POSITION_TYPE
     */
    public final POSITION_TYPE from(Position.Readable<? extends PARENT_TYPE> parent) {
        @SuppressWarnings("unchecked")
        POSITION_TYPE result =
            (POSITION_TYPE) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), mixins.keySet()
                .toArray(new Class[mixins.size()]), new RelativePositionInvocationHandler(parent.getValue(), mixins));
        return result;
    }
}
