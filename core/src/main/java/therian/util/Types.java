package therian.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.BindTypeVariable;
import therian.Typed;

public class Types {
    /**
     * @see Types#hierarchy(Class, Interfaces)
     */
    public enum Interfaces {
        INCLUDE, EXCLUDE;
    }

    private static final class GenericArrayTypeImpl implements GenericArrayType {
        private final Type componentType;

        private GenericArrayTypeImpl(Type componentType) {
            this.componentType = componentType;
        }

        @Override
        public Type getGenericComponentType() {
            return componentType;
        }

        @Override
        public String toString() {
            return Types.toString(this);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof GenericArrayType && Types.equals(this, (GenericArrayType) obj);
        }

        @Override
        public int hashCode() {
            int result = 67 << 4;
            result |= componentType.hashCode();
            return result;
        }
    }

    private static final class ParameterizedTypeImpl implements ParameterizedType {
        private final Class<?> raw;
        private final Type useOwner;
        private final Type[] typeArguments;

        private ParameterizedTypeImpl(Class<?> raw, Type useOwner, Type[] typeArguments) {
            this.raw = raw;
            this.useOwner = useOwner;
            this.typeArguments = typeArguments;
        }

        @Override
        public Type getRawType() {
            return raw;
        }

        @Override
        public Type getOwnerType() {
            return useOwner;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return typeArguments.clone();
        }

        @Override
        public String toString() {
            return Types.toString(this);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof ParameterizedType && Types.equals(this, ((ParameterizedType) obj));
        }

        @Override
        public int hashCode() {
            int result = 71 << 4;
            result |= raw.hashCode();
            result <<= 4;
            result |= ObjectUtils.hashCode(useOwner);
            result <<= 8;
            result |= Arrays.hashCode(typeArguments);
            return result;
        }
    }

    private static final class WildcardTypeImpl implements WildcardType {
        private final Type[] upperBounds;
        private final Type[] lowerBounds;

        private WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
            this.upperBounds = upperBounds;
            this.lowerBounds = lowerBounds;
        }

        @Override
        public Type[] getUpperBounds() {
            return upperBounds.clone();
        }

        @Override
        public Type[] getLowerBounds() {
            return lowerBounds.clone();
        }

        @Override
        public String toString() {
            return Types.toString(this);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof WildcardType && Types.equals(this, (WildcardType) obj);
        }

        @Override
        public int hashCode() {
            int result = 73 << 8;
            result |= Arrays.hashCode(upperBounds);
            result <<= 8;
            result |= Arrays.hashCode(lowerBounds);
            return result;
        }
    }

    private static final Map<Class<?>, Map<TypeVariable<?>, Method>> TYPED_GETTERS =
        new HashMap<Class<?>, Map<TypeVariable<?>, Method>>();

    /**
     * A wildcard instance matching {@code ?}.
     */
    public static final WildcardType WILDCARD_ALL = wildcardType(new Type[] { Object.class }, new Type[0]);

    /**
     * "Refine" a declared type:
     * <ul>
     * <li>If {@code type} is a {@link TypeVariable}, return its normalized upper bound.</li>
     * <li>If {@code type} is a {@link WildcardType}, return its normalized upper bound.</li>
     * </ul>
     *
     * @param type
     * @param parentType
     * @return Type
     */
    public static Type refine(Type type, Type parentType) {
        if (type instanceof TypeVariable) {
            return TypeUtils.normalizeUpperBounds(((TypeVariable<?>) type).getBounds())[0];
        }
        if (type instanceof WildcardType) {
            return TypeUtils.normalizeUpperBounds(((WildcardType) type).getUpperBounds())[0];
        }
        return type;
    }

    /**
     * Get a type representing {@code type} with variable assignments "unrolled."
     *
     * @param typeArguments as from {@link TypeUtils#getTypeArguments(Type, Class)}
     * @param type
     * @return Type
     */
    public static Type unrollVariables(Map<TypeVariable<?>, Type> typeArguments, final Type type) {
        typeArguments = TypeVariableMap.wrap(typeArguments);

        if (containsTypeVariables(type)) {
            if (type instanceof TypeVariable<?>) {
                return unrollVariables(typeArguments, typeArguments.get(type));
            }
            if (type instanceof ParameterizedType) {
                final ParameterizedType p = (ParameterizedType) type;
                final Map<TypeVariable<?>, Type> parameterizedTypeArguments;
                if (p.getOwnerType() == null) {
                    parameterizedTypeArguments = typeArguments;
                } else {
                    parameterizedTypeArguments = new HashMap<TypeVariable<?>, Type>(typeArguments);
                    parameterizedTypeArguments.putAll(TypeUtils.getTypeArguments(p));
                }
                final Type[] args = p.getActualTypeArguments();
                for (int i = 0; i < args.length; i++) {
                    final Type unrolled = unrollVariables(parameterizedTypeArguments, args[i]);
                    if (unrolled != null) {
                        args[i] = unrolled;
                    }
                }
                return parameterizeWithOwner(p.getOwnerType(), (Class<?>) p.getRawType(), args);
            }
            if (type instanceof WildcardType) {
                final WildcardType wild = (WildcardType) type;
                return wildcardType(unrollBounds(typeArguments, wild.getUpperBounds()),
                    unrollBounds(typeArguments, wild.getLowerBounds()));
            }
        }
        return type;
    }

    private static Type[] unrollBounds(final Map<TypeVariable<?>, Type> typeArguments, final Type[] bounds) {
        Type[] result = bounds;
        int i = 0;
        for (; i < result.length; i++) {
            final Type unrolled = unrollVariables(typeArguments, result[i]);
            if (unrolled == null) {
                result = ArrayUtils.remove(result, i--);
            } else {
                result[i] = unrolled;
            }
        }
        return result;
    }

    /**
     * Learn, recursively, whether any of the type parameters associated with {@code type} are bound to variables.
     *
     * @param type
     * @return boolean
     */
    public static boolean containsTypeVariables(Type type) {
        if (type instanceof TypeVariable<?>) {
            return true;
        }
        if (type instanceof Class<?>) {
            return ((Class<?>) type).getTypeParameters().length > 0;
        }
        if (type instanceof ParameterizedType) {
            for (Type arg : ((ParameterizedType) type).getActualTypeArguments()) {
                if (containsTypeVariables(arg)) {
                    return true;
                }
            }
            return false;
        }
        if (type instanceof WildcardType) {
            WildcardType wild = (WildcardType) type;
            return containsTypeVariables(TypeUtils.getImplicitLowerBounds(wild)[0])
                || containsTypeVariables(TypeUtils.getImplicitUpperBounds(wild)[0]);
        }
        return false;
    }

    /**
     * Create a parameterized type instance.
     *
     * @param raw
     * @param typeArguments
     * @return {@link ParameterizedType}
     */
    public static final ParameterizedType parameterize(final Class<?> raw, final Type... typeArguments) {
        return parameterizeWithOwner(null, raw, typeArguments);
    }

    /**
     * Create a parameterized type instance.
     *
     * @param raw
     * @param typeArgMappings
     * @return {@link ParameterizedType}
     */
    public static final ParameterizedType parameterize(final Class<?> raw,
        final Map<TypeVariable<?>, Type> typeArgMappings) {
        return parameterizeWithOwner(null, raw, extractTypeArgumentsFrom(typeArgMappings, raw.getTypeParameters()));
    }

    /**
     * Create a parameterized type instance.
     *
     * @param owner
     * @param raw
     * @param typeArguments
     *
     * @return {@link ParameterizedType}
     */
    public static final ParameterizedType parameterizeWithOwner(final Type owner, final Class<?> raw,
        final Type... typeArguments) {
        Validate.notNull(raw, "raw class");
        final Type useOwner;
        if (raw.getEnclosingClass() == null) {
            Validate.isTrue(owner == null, "no owner allowed for top-level %s", raw);
            useOwner = null;
        } else if (owner == null) {
            useOwner = raw.getEnclosingClass();
        } else {
            Validate.isTrue(TypeUtils.isAssignable(owner, raw.getEnclosingClass()),
                "%s is invalid owner type for parameterized %s", owner, raw);
            useOwner = owner;
        }
        Validate.isTrue(raw.getTypeParameters().length == Validate.noNullElements(typeArguments,
            "null type argument at index %s").length);

        return new ParameterizedTypeImpl(raw, useOwner, typeArguments);
    }

    /**
     * Create a parameterized type instance.
     *
     * @param owner
     * @param raw
     * @param typeArgMappings
     * @return {@link ParameterizedType}
     */
    public static final ParameterizedType parameterizeWithOwner(final Type owner, final Class<?> raw,
        final Map<TypeVariable<?>, Type> typeArgMappings) {
        return parameterizeWithOwner(owner, raw, extractTypeArgumentsFrom(typeArgMappings, raw.getTypeParameters()));
    }

    private static Type[] extractTypeArgumentsFrom(Map<TypeVariable<?>, Type> mappings, TypeVariable<?>[] variables) {
        mappings = TypeVariableMap.wrap(mappings);

        final Type[] result = new Type[variables.length];
        int index = 0;
        for (TypeVariable<?> var : variables) {
            Validate.isTrue(mappings.containsKey(var), "missing argument mapping for %s", toString(var));
            result[index++] = mappings.get(var);
        }
        return result;
    }

    /**
     * Create a wildcard type instance.
     *
     * @param upperBounds
     * @param lowerBounds
     * @return WildcardType
     */
    public static WildcardType wildcardType(final Type[] upperBounds, final Type[] lowerBounds) {
        Validate.noNullElements(upperBounds, "upperBounds contains null at index %s");
        Validate.notEmpty(upperBounds, "upperBounds is empty");
        Validate.noNullElements(lowerBounds, "lowerBounds contains null at index %s");

        return new WildcardTypeImpl(upperBounds, lowerBounds);
    }

    /**
     * Create a generic array type instance.
     *
     * @param componentType
     * @return {@link GenericArrayType}
     */
    public static GenericArrayType genericArrayType(final Type componentType) {
        return new GenericArrayTypeImpl(componentType);
    }

    /**
     * Tries to "read" a {@link TypeVariable} from an object instance, taking into account {@link BindTypeVariable} and
     * {@link Typed} before falling back to basic type
     *
     * @param o
     * @param var
     * @return Type resolved or {@code null}
     */
    public static Type resolveAt(Object o, TypeVariable<?> var) {
        final Class<?> rt = Validate.notNull(o, "null target").getClass();
        final String v = String.format("Type variable [%s].<%s>", var.getGenericDeclaration(), var.getName());
        Validate.isTrue(var.getGenericDeclaration() instanceof Class<?>, "%s is not declared by a Class", v);
        final Class<?> declaring = (Class<?>) var.getGenericDeclaration();
        Validate.isTrue(declaring.isInstance(o), "%s does not belong to %s", v, rt);

        for (Class<?> c : init(rt)) {
            final Map<TypeVariable<?>, Method> gettersForType = TYPED_GETTERS.get(c);
            if (gettersForType != null && gettersForType.containsKey(var)) {
                return readTyped(gettersForType.get(var), o);
            }
        }
        return unrollVariables(TypeUtils.getTypeArguments(o.getClass(), declaring), var);
    }

    private static Type readTyped(Method method, Object target) {
        try {
            final Typed<?> typed = (Typed<?>) method.invoke(target);
            Validate.validState(typed != null, "%s returned null", method);
            return typed.getType();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Iterable<Class<?>> init(Class<?> c) {
        final Iterable<Class<?>> result = hierarchy(c, Interfaces.INCLUDE);
        if (!TYPED_GETTERS.containsKey(c)) {
            final VariableWalker varWalker = new VariableWalker(c);
            init(varWalker, result.iterator());
        }
        return result;
    }

    private static void init(VariableWalker varWalker, Iterator<Class<?>> types) {
        if (types.hasNext()) {
            final Class<?> c = types.next();
            synchronized (TYPED_GETTERS) {
                Map<TypeVariable<?>, Method> m = TYPED_GETTERS.get(c);
                if (m == null) {
                    m = new HashMap<TypeVariable<?>, Method>();
                    putTypedGetters(m, c);
                    TYPED_GETTERS.put(c, m.isEmpty() ? Collections.<TypeVariable<?>, Method> emptyMap() : m);
                }
                if (!m.isEmpty()) {
                    varWalker.expandMappings(m);
                }
                init(varWalker, types);
            }
        }
    }

    private static class VariableWalker {
        final Map<TypeVariable<?>, Type> assignments;
        final Map<TypeVariable<?>, Type> inverseAssignments;

        VariableWalker(Type t) {
            assignments = getAllAssignments(t);
            inverseAssignments = invert(assignments);
        }

        void expandMappings(Map<TypeVariable<?>, Method> m) {
            final Map<TypeVariable<?>, Method> additionalMappings = new HashMap<TypeVariable<?>, Method>();
            for (Map.Entry<TypeVariable<?>, Method> e : m.entrySet()) {
                traverseAssignments(additionalMappings, e, assignments);
                traverseAssignments(additionalMappings, e, inverseAssignments);
            }
            m.putAll(additionalMappings);
        }

        void traverseAssignments(Map<TypeVariable<?>, Method> target, Map.Entry<TypeVariable<?>, Method> e,
            Map<TypeVariable<?>, Type> source) {

            Type t = source.get(e.getKey());
            while (t instanceof TypeVariable<?>) {
                final TypeVariable<?> v = (TypeVariable<?>) t;
                target.put(v, e.getValue());
                t = source.get(v);
            }
        }
    }

    private static void putTypedGetters(Map<TypeVariable<?>, Method> intoMap, Class<?> type) {
        for (Method m : type.getDeclaredMethods()) {
            if (m.getAnnotation(BindTypeVariable.class) == null) {
                continue;
            }
            final String ms = String.format("%s method %s", BindTypeVariable.class.getName(), m);

            Validate.isTrue(m.getParameterTypes().length == 0, "%s must accept 0 parameters", ms);
            Validate.isTrue(Typed.class.isAssignableFrom(m.getReturnType()), "%s must return %s", ms,
                Typed.class.getName());
            final Type param =
                TypeUtils.getTypeArguments(m.getGenericReturnType(), Typed.class).get(
                    Typed.class.getTypeParameters()[0]);
            Validate.isTrue(param instanceof TypeVariable<?>
                && ((TypeVariable<?>) param).getGenericDeclaration().equals(type),
                "%s should bind a class type parameter to %s.<%s>", ms, Typed.class.getName(),
                Typed.class.getTypeParameters()[0].getName());
            intoMap.put((TypeVariable<?>) param, m);
        }
    }

    /**
     * Get an {@link Iterable} that can iterate over a class hierarchy in ascending (subclass to superclass) order,
     * excluding interfaces.
     *
     * @param type
     * @return Iterable
     */
    public static Iterable<Class<?>> hierarchy(final Class<?> type) {
        return hierarchy(type, Interfaces.EXCLUDE);
    }

    /**
     * Get an {@link Iterable} that can iterate over a class hierarchy in ascending (subclass to superclass) order.
     *
     * @param type
     * @param interfacesBehavior
     * @return Iterable
     */
    public static Iterable<Class<?>> hierarchy(final Class<?> type, Interfaces interfacesBehavior) {
        final Iterable<Class<?>> classes = new Iterable<Class<?>>() {

            @Override
            public Iterator<Class<?>> iterator() {
                final MutableObject<Class<?>> next = new MutableObject<Class<?>>(type);
                return new Iterator<Class<?>>() {

                    @Override
                    public boolean hasNext() {
                        return next.getValue() != null;
                    }

                    @Override
                    public Class<?> next() {
                        final Class<?> result = next.getValue();
                        next.setValue(result.getSuperclass());
                        return result;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                };
            }

        };
        if (interfacesBehavior != Interfaces.INCLUDE) {
            return classes;
        }
        return new Iterable<Class<?>>() {

            @Override
            public Iterator<Class<?>> iterator() {
                final Set<Class<?>> seenInterfaces = new HashSet<Class<?>>();
                final Iterator<Class<?>> wrapped = classes.iterator();

                return new Iterator<Class<?>>() {
                    Iterator<Class<?>> interfaces = Collections.<Class<?>> emptySet().iterator();

                    @Override
                    public boolean hasNext() {
                        return interfaces.hasNext() || wrapped.hasNext();
                    }

                    @Override
                    public Class<?> next() {
                        if (interfaces.hasNext()) {
                            final Class<?> nextInterface = interfaces.next();
                            seenInterfaces.add(nextInterface);
                            return nextInterface;
                        }
                        final Class<?> nextSuperclass = wrapped.next();
                        final Set<Class<?>> currentInterfaces = new LinkedHashSet<Class<?>>();
                        walkInterfaces(currentInterfaces, nextSuperclass);
                        interfaces = currentInterfaces.iterator();
                        return nextSuperclass;
                    }

                    private void walkInterfaces(Set<Class<?>> addTo, Class<?> c) {
                        for (Class<?> iface : c.getInterfaces()) {
                            addTo.add(iface);
                            walkInterfaces(addTo, iface);
                        }
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                };
            }
        };
    }

    /**
     * Check equality of types.
     *
     * @param t1
     * @param t2
     * @return boolean
     */
    public static boolean equals(Type t1, Type t2) {
        if (ObjectUtils.equals(t1, t2)) {
            return true;
        }
        if (t1 instanceof ParameterizedType) {
            return equals((ParameterizedType) t1, t2);
        }
        if (t1 instanceof GenericArrayType) {
            return equals((GenericArrayType) t1, t2);
        }
        if (t1 instanceof WildcardType) {
            return equals((WildcardType) t1, t2);
        }
        return false;
    }

    private static boolean equals(ParameterizedType p, Type t) {
        if (t instanceof ParameterizedType) {
            final ParameterizedType other = (ParameterizedType) t;
            if (equals(p.getRawType(), other.getRawType()) && equals(p.getOwnerType(), other.getOwnerType())) {
                return equals(p.getActualTypeArguments(), other.getActualTypeArguments());
            }
        }
        return false;
    }

    private static boolean equals(GenericArrayType a, Type t) {
        return t instanceof GenericArrayType
            && equals(a.getGenericComponentType(), ((GenericArrayType) t).getGenericComponentType());
    }

    private static boolean equals(WildcardType w, Type t) {
        if (t instanceof WildcardType) {
            final WildcardType other = (WildcardType) t;
            return equals(w.getLowerBounds(), other.getLowerBounds())
                && equals(TypeUtils.getImplicitUpperBounds(w), TypeUtils.getImplicitUpperBounds(other));
        }
        return true;
    }

    private static boolean equals(Type[] t1, Type[] t2) {
        if (t1.length == t2.length) {
            for (int i = 0; i < t1.length; i++) {
                if (!equals(t1[i], t2[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Present a given type as a Java-esque String.
     *
     * @param type
     * @return String
     */
    public static String toString(Type type) {
        Validate.notNull(type);
        if (type instanceof Class<?>) {
            return classToString((Class<?>) type);
        }
        if (type instanceof ParameterizedType) {
            return parameterizedTypeToString((ParameterizedType) type);
        }
        if (type instanceof WildcardType) {
            return wildcardTypeToString((WildcardType) type);
        }
        if (type instanceof TypeVariable<?>) {
            return typeVariableToString((TypeVariable<?>) type);
        }
        if (type instanceof GenericArrayType) {
            return genericArrayTypeToString((GenericArrayType) type);
        }
        throw new IllegalArgumentException(ObjectUtils.identityToString(type));
    }

    /**
     * Format a {@link TypeVariable} including its {@link GenericDeclaration}.
     *
     * @param var
     * @return String
     */
    public static String toLongString(TypeVariable<?> var) {
        final StringBuilder buf = new StringBuilder();
        final GenericDeclaration d = ((TypeVariable<?>) var).getGenericDeclaration();
        if (d instanceof Class<?>) {
            Class<?> c = (Class<?>) d;
            while (true) {
                if (c.getEnclosingClass() == null) {
                    buf.insert(0, c.getName());
                    break;
                }
                buf.insert(0, c.getSimpleName()).insert(0, '.');
                c = c.getEnclosingClass();
            }
        } else if (d instanceof Type) {// not possible as of now
            buf.append(toString((Type) d));
        } else {
            buf.append(d);
        }
        return buf.append(':').append(typeVariableToString(var)).toString();
    }

    /**
     * Wrap the specified {@link Type} in a {@link Typed} wrapper.
     *
     * @param type to wrap
     * @return Typed<?>
     */
    @SuppressWarnings("rawtypes")
    public static Typed<?> wrap(final Type type) {
        return new Typed() {
            @Override
            public Type getType() {
                return type;
            }
        };
    }

    /**
     * Wrap the specified {@link Class} in a {@link Typed} wrapper.
     *
     * @param type to wrap
     * @return Typed<T>
     */
    public static <T> Typed<T> wrap(final Class<T> type) {
        @SuppressWarnings("unchecked")
        final Typed<T> result = (Typed<T>) wrap((Type) type);
        return result;
    }

    private static String classToString(Class<?> c) {
        final StringBuilder buf = new StringBuilder();

        if (c.getEnclosingClass() != null) {
            buf.append(classToString(c.getEnclosingClass())).append('.').append(c.getSimpleName());
        } else {
            buf.append(c.getName());
        }
        if (c.getTypeParameters().length > 0) {
            buf.append('<');
            appendAllTo(buf, ", ", c.getTypeParameters());
            buf.append('>');
        }
        return buf.toString();
    }

    private static String typeVariableToString(TypeVariable<?> v) {
        final StringBuilder buf = new StringBuilder(v.getName());
        final Type[] bounds = v.getBounds();
        if (bounds.length > 0 && !(bounds.length == 1 && Object.class.equals(bounds[0]))) {
            buf.append(" extends ");
            appendAllTo(buf, " & ", v.getBounds());
        }
        return buf.toString();
    }

    private static String parameterizedTypeToString(ParameterizedType p) {
        final StringBuilder buf = new StringBuilder();

        final Type useOwner = p.getOwnerType();
        final Class<?> raw = (Class<?>) p.getRawType();
        final Type[] typeArguments = p.getActualTypeArguments();
        if (useOwner == null) {
            buf.append(raw.getName());
        } else {
            if (useOwner instanceof Class<?>) {
                buf.append(((Class<?>) useOwner).getName());
            } else {
                buf.append(useOwner.toString());
            }
            buf.append('.').append(raw.getSimpleName());
        }

        appendAllTo(buf.append('<'), ", ", typeArguments).append('>');
        return buf.toString();
    }

    private static String wildcardTypeToString(WildcardType w) {
        final StringBuilder buf = new StringBuilder().append('?');
        final Type[] lowerBounds = w.getLowerBounds();
        final Type[] upperBounds = w.getUpperBounds();
        if (lowerBounds.length > 0) {
            appendAllTo(buf.append(" super "), " & ", lowerBounds);
        } else if (!(upperBounds.length == 1 && Object.class.equals(upperBounds[0]))) {
            appendAllTo(buf.append(" extends "), " & ", upperBounds);
        }
        return buf.toString();
    }

    private static String genericArrayTypeToString(GenericArrayType g) {
        return String.format("%s[]", toString(g.getGenericComponentType()));
    }

    private static StringBuilder appendAllTo(StringBuilder buf, String sep, Type... types) {
        Validate.notEmpty(Validate.noNullElements(types));
        if (types.length > 0) {
            buf.append(toString(types[0]));
            for (int i = 1; i < types.length; i++) {
                buf.append(sep).append(toString(types[i]));
            }
        }
        return buf;
    }

    /**
     * Get a map of all {@link TypeVariable} assignments for a given type.
     *
     * @param t
     * @return Map<TypeVariable<?>, Type>
     */
    private static Map<TypeVariable<?>, Type> getAllAssignments(final Type t) {
        return spider(new TypeVariableMap(), new HashSet<Class<?>>(), t);
    }

    private static Map<TypeVariable<?>, Type> spider(final Map<TypeVariable<?>, Type> result,
        final Set<Class<?>> seenInterfaces, final Type t) {
        if (t == null) {
            return result;
        }
        final Class<?> raw;
        if (t instanceof ParameterizedType) {
            final ParameterizedType p = (ParameterizedType) t;
            raw = (Class<?>) p.getRawType();
            final Type[] args = p.getActualTypeArguments();
            final TypeVariable<?>[] vars = raw.getTypeParameters();
            for (int i = 0; i < vars.length; i++) {
                result.put(vars[i], args[i]);
            }
        } else {
            raw = (Class<?>) t;
        }
        synchronized (seenInterfaces) {
            if (raw.isInterface() && !seenInterfaces.add(raw)) {
                return result;
            }
        }
        for (Type nterface : raw.getGenericInterfaces()) {
            spider(result, seenInterfaces, nterface);
        }
        return spider(result, seenInterfaces, raw.getGenericSuperclass());
    }

    /**
     * Return the inverse map of {@code m} for all entries whose value is a {@link TypeVariable}.
     *
     * @param m
     * @return Map<TypeVariable<?>, Type>
     */
    private static Map<TypeVariable<?>, Type> invert(Map<TypeVariable<?>, Type> m) {
        final TypeVariableMap result = new TypeVariableMap();
        for (Map.Entry<TypeVariable<?>, Type> e : m.entrySet()) {
            if (e.getValue() instanceof TypeVariable<?>) {
                result.put((TypeVariable<?>) e.getValue(), e.getKey());
            }
        }
        return result;
    }
}
