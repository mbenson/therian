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
 * {@link RelativePosition} factory.
 */
public abstract class RelativePositionFactory<P, T, POSITION_TYPE extends Position<T>> {
    private static final Class<?>[] TO_IMPLEMENT_BASE = { RelativePosition.class };

    private class RelativePositionInvocationHandler implements InvocationHandler {
        final Position.Readable<? extends P> parentPosition;
        final Map<Class<? extends Position<?>>, Mixin<P, T>> mixins;

        protected RelativePositionInvocationHandler(Position.Readable<? extends P> parentPosition,
            Map<Class<? extends Position<?>>, Mixin<P, T>> mixins) {
            super();
            this.parentPosition = parentPosition;
            this.mixins = mixins;
        }

        /**
         * {@inheritDoc}
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Mixin<P, T> mixin = mixins.get(method.getDeclaringClass());
            if (mixin != null) {
                return MethodUtils.invokeMethod(mixin, method.getName(), ArrayUtils.add(args, 0, parentPosition));
            }
            if (method.equals(RelativePosition.class.getMethod("getParentPosition"))) {
                return parentPosition;
            }
            if (method.equals(Object.class.getMethod("equals", Object.class))) {
                return args[0].getClass().equals(proxy.getClass()) && Proxy.getInvocationHandler(args[0]).equals(this);
            }
            if (method.equals(Object.class.getMethod("hashCode"))) {
                return System.identityHashCode(proxy);
            }
            if (method.equals(Object.class.getMethod("toString"))) {
                return String.format("Relative Position: %s of %s", RelativePositionFactory.this.getClass()
                    .getSimpleName(), parentPosition);
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
            if (obj instanceof RelativePositionFactory.RelativePositionInvocationHandler == false) {
                return false;
            }
            @SuppressWarnings("rawtypes")
            RelativePositionFactory.RelativePositionInvocationHandler other =
                (RelativePositionFactory.RelativePositionInvocationHandler) obj;
            return ObjectUtils.equals(parentPosition, other.parentPosition) && ObjectUtils.equals(mixins, other.mixins);
        }
    }

    private final Map<Class<? extends Position<?>>, Mixin<P, T>> mixins;

    /**
     * Create a new RelativePositionFactory instance.
     * 
     * @param mixins
     */
    protected RelativePositionFactory(Mixin<P, T>... mixins) {
        super();
        final Map<Class<? extends Position<?>>, Mixin<P, T>> m =
            new HashMap<Class<? extends Position<?>>, RelativePosition.Mixin<P, T>>();
        for (Mixin<P, T> mixin : mixins) {
            for (Class<? extends Position<?>> positionType : getImplementedTypes(mixin.getClass())) {
                if (m.containsKey(positionType)) {
                    throw new IllegalArgumentException(String.format("%s implemented by > 1 of mixins %s",
                        positionType, mixins));
                }
                m.put(positionType, mixin);
            }
        }
        this.mixins = Collections.unmodifiableMap(m);
    }

    private static Class<? extends Position<?>>[] getImplementedTypes(
        @SuppressWarnings("rawtypes") Class<? extends Mixin> mixinType) {
        final HashSet<Class<? extends Position<?>>> set = new HashSet<Class<? extends Position<?>>>();
        for (Class<?> iface : mixinType.getInterfaces()) {
            Implements annotation = iface.getAnnotation(Implements.class);
            if (annotation != null) {
                @SuppressWarnings("unchecked")
                final Class<? extends Position<?>>[] classes = (Class<? extends Position<?>>[]) annotation.value();
                Collections.addAll(set, classes);
            }
        }
        @SuppressWarnings("unchecked")
        final Class<? extends Position<?>>[] result = set.toArray(new Class[set.size()]);
        return result;
    }

    /**
     * Get a {@link Position} relative to {@code parent}.
     * 
     * @param parent
     * @return POSITION_TYPE
     */
    public final POSITION_TYPE of(Position.Readable<? extends P> parentPosition) {
        @SuppressWarnings("unchecked")
        final POSITION_TYPE result =
            (POSITION_TYPE) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                // abusive, but works:
                (Class<?>[]) ArrayUtils.addAll(TO_IMPLEMENT_BASE, mixins.keySet().toArray()),
                new RelativePositionInvocationHandler(parentPosition, mixins));
        return result;
    }
}
