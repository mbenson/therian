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

public class RelativePositionFactory<PARENT, TYPE> {
    private class RelativePositionInvocationHandler implements InvocationHandler {
        final Position.Readable<? extends PARENT> parentPosition;
        final Map<Class<? extends Position<?>>, RelativePosition.Mixin<TYPE>> mixins;

        protected RelativePositionInvocationHandler(Position.Readable<? extends PARENT> parentPosition,
            Map<Class<? extends Position<?>>, RelativePosition.Mixin<TYPE>> mixins) {
            super();
            this.parentPosition = parentPosition;
            this.mixins = mixins;
        }

        /**
         * {@inheritDoc}
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final RelativePosition.Mixin<TYPE> mixin = mixins.get(method.getDeclaringClass());
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

        private RelativePositionFactory<PARENT, TYPE> getRelativePositionFactory() {
            return RelativePositionFactory.this;
        }
    }

    private static final Comparator<Class<?>> CLASS_COMPARATOR = new Comparator<Class<?>>() {

        public int compare(Class<?> o1, Class<?> o2) {
            return o1.isAssignableFrom(o2) ? -1 : o2.isAssignableFrom(o1) ? 1 : 0;
        }
    };

    private final Map<Class<? extends Position<?>>, RelativePosition.Mixin<TYPE>> mixins;

    protected RelativePositionFactory(RelativePosition.Mixin<TYPE>... mixins) {
        super();
        final Map<Class<? extends Position<?>>, RelativePosition.Mixin<TYPE>> m =
            new HashMap<Class<? extends Position<?>>, RelativePosition.Mixin<TYPE>>();
        for (RelativePosition.Mixin<TYPE> mixin : mixins) {
            addTo(m, mixin);
        }
        if (!m.containsKey(RelativePosition.class)) {
            addTo(m, new RelativePosition.Mixin.GetParentPosition<TYPE>());
        }
        this.mixins = Collections.unmodifiableMap(m);
    }

    private void addTo(final Map<Class<? extends Position<?>>, RelativePosition.Mixin<TYPE>> target,
        final RelativePosition.Mixin<TYPE> mixin) {
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
    public <P extends PARENT> RelativePosition<P, TYPE> of(Position.Readable<P> parentPosition) {
        @SuppressWarnings("unchecked")
        final RelativePosition<P, TYPE> result =
            (RelativePosition<P, TYPE>) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                gatherInterfaces(), new RelativePositionInvocationHandler(parentPosition, mixins));
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
