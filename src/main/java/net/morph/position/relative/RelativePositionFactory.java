package net.morph.position.relative;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

import net.morph.position.Position;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

public class RelativePositionFactory<T> {
    private class RelativePositionInvocationHandler<P> implements InvocationHandler {
        final Position.Readable<? extends P> parentPosition;
        final Map<Class<? extends Position<?>>, RelativePosition.Mixin<T>> mixins;

        protected RelativePositionInvocationHandler(Position.Readable<? extends P> parentPosition,
            Map<Class<? extends Position<?>>, RelativePosition.Mixin<T>> mixins) {
            super();
            this.parentPosition = parentPosition;
            this.mixins = mixins;
        }

        /**
         * {@inheritDoc}
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final RelativePosition.Mixin<T> mixin = mixins.get(method.getDeclaringClass());
            if (mixin != null) {
                return MethodUtils.invokeMethod(mixin, method.getName(), ArrayUtils.add(args, 0, parentPosition));
            }
            if (method.equals(Object.class.getMethod("equals", Object.class))) {
                return Proxy.getInvocationHandler(args[0]).equals(this);
            }
            if (method.equals(Object.class.getMethod("hashCode"))) {
                int result = 61 << 4;
                result |= this.hashCode();
                return result;
            }
            if (method.equals(Object.class.getMethod("toString"))) {
                return String.format("Relative Position: %s of %s", RelativePositionFactory.this.getClass()
                    .getSimpleName(), parentPosition);
            }
            throw new UnsupportedOperationException(String.format("%s %s(%s)", method.getReturnType().getName(),
                method.getName(), ArrayUtils.toString(args, "")));
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
            final RelativePositionFactory.RelativePositionInvocationHandler other =
                (RelativePositionFactory.RelativePositionInvocationHandler) obj;
            return parentPosition.equals(other.parentPosition)
                && getRelativePositionFactory().equals(other.getRelativePositionFactory());
        }

        @Override
        public int hashCode() {
            int result = 53 << 4;
            result |= parentPosition.hashCode();
            result <<= 4;
            result |= getRelativePositionFactory().hashCode();
            return result;
        }

        private RelativePositionFactory<T> getRelativePositionFactory() {
            return RelativePositionFactory.this;
        }
    }

    private static final Comparator<Class<?>> CLASS_COMPARATOR = new Comparator<Class<?>>() {

        public int compare(Class<?> o1, Class<?> o2) {
            return o1.isAssignableFrom(o2) ? -1 : o2.isAssignableFrom(o1) ? 1 : 0;
        }
    };

    private final Map<Class<? extends Position<?>>, RelativePosition.Mixin<T>> mixins;

    protected RelativePositionFactory(RelativePosition.Mixin<T>... mixins) {
        super();
        final Map<Class<? extends Position<?>>, RelativePosition.Mixin<T>> m =
            new HashMap<Class<? extends Position<?>>, RelativePosition.Mixin<T>>();
        for (RelativePosition.Mixin<T> mixin : mixins) {
            addTo(m, mixin);
        }
        if (!m.containsKey(RelativePosition.class)) {
            addTo(m, new RelativePosition.Mixin.GetParentPosition<T>());
        }
        this.mixins = Collections.unmodifiableMap(m);
    }

    private void addTo(final Map<Class<? extends Position<?>>, RelativePosition.Mixin<T>> target,
        final RelativePosition.Mixin<T> mixin) {
        for (Class<? extends Position<?>> positionType : RelativePosition.Mixin.HELPER.getImplementedTypes(mixin)) {
            if (target.containsKey(positionType)) {
                throw new IllegalArgumentException(String.format("%s implemented by > 1 of mixins %s", positionType,
                    mixins));
            }
            target.put(positionType, mixin);
        }
    }

    /**
     * Obtain the {@link RelativePosition}.
     * 
     * @param parentPosition
     * @return {@link RelativePosition}
     */
    public <P> RelativePosition<P, T> of(Position.Readable<P> parentPosition) {
        @SuppressWarnings("unchecked")
        final RelativePosition<P, T> result =
            (RelativePosition<P, T>) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                gatherInterfaces(), new RelativePositionInvocationHandler<P>(parentPosition, mixins));
        return result;
    }

    private Class<?>[] gatherInterfaces() {
        final HashSet<Class<?>> interfaces = new HashSet<Class<?>>();
        interfaces.add(RelativePosition.class);

        for (Class<?> iface : mixins.keySet()) {
            interfaces.add(iface);
            interfaces.addAll(ClassUtils.getAllInterfaces(iface));
        }
        final TreeSet<Class<?>> sortedCompositeTypes = new TreeSet<Class<?>>(CLASS_COMPARATOR);
        Collections.addAll(sortedCompositeTypes, getCompositeInterfaces());

        for (Class<?> compositeType : sortedCompositeTypes) {
            if (interfaces.containsAll(ClassUtils.getAllInterfaces(compositeType))) {
                interfaces.add(compositeType);
            }
        }
        return interfaces.toArray(ArrayUtils.EMPTY_CLASS_ARRAY);
    }

    /**
     * Helper to get known composite interfaces. Most implementors should never have to override this.
     * 
     * @return Class[]
     */
    @SuppressWarnings({ "rawtypes" })
    protected Class<? extends Position>[] getCompositeInterfaces() {
        @SuppressWarnings("unchecked")
        Class<? extends Position>[] result =
            ArrayUtils.toArray(Position.ReadWrite.class, RelativePosition.ReadWrite.class);
        return result;
    }

}
