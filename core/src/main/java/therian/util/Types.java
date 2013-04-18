package therian.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.BindTypeVariable;
import therian.Typed;

public class Types {
    private static final class GenericArrayTypeImpl implements GenericArrayType {
        private final Type componentType;

        private GenericArrayTypeImpl(Type componentType) {
            this.componentType = componentType;
        }

        public Type getGenericComponentType() {
            return componentType;
        }

        @Override
        public String toString() {
            return Types.toString(this);
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

        public Type getRawType() {
            return raw;
        }

        public Type getOwnerType() {
            return useOwner;
        }

        public Type[] getActualTypeArguments() {
            return typeArguments.clone();
        }

        @Override
        public String toString() {
            return Types.toString(this);
        }
    }

    private static final class WildcardTypeImpl implements WildcardType {
        private final Type[] upperBounds;
        private final Type[] lowerBounds;

        private WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
            this.upperBounds = upperBounds;
            this.lowerBounds = lowerBounds;
        }

        public Type[] getUpperBounds() {
            return upperBounds.clone();
        }

        public Type[] getLowerBounds() {
            return lowerBounds.clone();
        }

        @Override
        public String toString() {
            return Types.toString(this);
        }
    }

    private static final Map<Class<?>, Map<TypeVariable<?>, Method>> TYPED_GETTERS =
        new HashMap<Class<?>, Map<TypeVariable<?>, Method>>();

    /**
     * A wildcard instance matching {@code ?}.
     */
    public static final WildcardType WILDCARD_ALL = wildcardType(new Type[] { Object.class }, new Type[0]);

    public static Type refine(Type type, Type parentType) {
        if (type instanceof TypeVariable) {
            return TypeUtils.normalizeUpperBounds(((TypeVariable<?>) type).getBounds())[0];
        }
        return type;
    }

    /**
     * Get a type representing {@code type} with variable assignments "unrolled."
     * 
     * @param typeArguments
     *            as from {@link TypeUtils#getTypeArguments(Type, Class)}
     * @param type
     * @return Type
     */
    public static Type unrollVariables(final Map<TypeVariable<?>, Type> typeArguments, final Type type) {
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
     * @return ParameterizedType
     */
    public static final ParameterizedType parameterize(final Class<?> raw, final Type... typeArguments) {
        return parameterizeWithOwner(null, raw, typeArguments);
    }

    /**
     * Create a parameterized type instance.
     * 
     * @param owner
     * @param raw
     * @param typeArguments
     * 
     * @return ParameterizedType
     */
    public static final ParameterizedType parameterizeWithOwner(Type owner, final Class<?> raw,
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
     * Tries to "read" a {@link TypeVariable} from an object instance,
     * taking into account {@link BindTypeVariable} and {@link Typed} before falling back to basic type
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

        init(rt);

        for (Class<?> c : hierarchy(rt)) {
            Method m = getTypedMethod(o, c, var);
            if (m != null) {
                return readTyped(m, o);
            }
        }
        return unrollVariables(new HashMap<TypeVariable<?>, Type>(TypeUtils.getTypeArguments(o.getClass(), declaring)),
            var);
    }

    private static Method getTypedMethod(Object o, Class<?> type, TypeVariable<?> var) {
        if (TYPED_GETTERS.containsKey(type)) {
            final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(o.getClass(), type);
            for (Map.Entry<TypeVariable<?>, Method> e : TYPED_GETTERS.get(type).entrySet()) {
                Type t = e.getKey();
                while (t instanceof TypeVariable<?>) {
                    if (t.equals(var)) {
                        return e.getValue();
                    }
                    if (typeArguments == null) {
                        break;
                    }
                    t = typeArguments.get(t);
                }
            }
        }
        return null;
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

    private static Class<?> init(Class<?> type) {
        if (type != null) {
            init(type.getSuperclass());

            synchronized (type) {
                if (!TYPED_GETTERS.containsKey(type)) {
                    TYPED_GETTERS.put(type, typedGetters(type));
                }
            }
        }
        return type;
    }

    private static Map<TypeVariable<?>, Method> typedGetters(Class<?> type) {
        final Map<TypeVariable<?>, Method> result = new HashMap<TypeVariable<?>, Method>();
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
            result.put((TypeVariable<?>) param, m);
        }
        return result.isEmpty() ? Collections.<TypeVariable<?>, Method> emptyMap() : Collections
            .unmodifiableMap(result);
    }

    /**
     * Get an {@link Iterable} that can iterate over a class hierarchy in ascending (subclass to superclass) order.
     * 
     * @param type
     * @return Iterable
     */
    public static Iterable<Class<?>> hierarchy(final Class<?> type) {
        return new Iterable<Class<?>>() {

            public Iterator<Class<?>> iterator() {
                final MutableObject<Class<?>> next = new MutableObject<Class<?>>(type);
                return new Iterator<Class<?>>() {

                    public boolean hasNext() {
                        return next.getValue() != null;
                    }

                    public Class<?> next() {
                        final Class<?> result = next.getValue();
                        next.setValue(result.getSuperclass());
                        return result;
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                };
            }

        };
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

    private static String classToString(Class<?> c) {
        final StringBuilder buf = new StringBuilder();

        if (c.getSuperclass() != null) {
            buf.append(classToString(c.getSuperclass())).append('.').append(c.getSimpleName());
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
}
