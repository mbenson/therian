package therian.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.Typed;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.BindTypeVariable;

public class Types {

    private static final Map<Class<?>, Map<TypeVariable<?>, Method>> TYPED_GETTERS =
        new HashMap<Class<?>, Map<TypeVariable<?>, Method>>();

    // borrowed from Commons Lang MemberUtils
    private static final int ACCESS_TEST = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;

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
     * Tries to "read" a {@link TypeVariable} from an object instance, taking into account {@link BindTypeVariable} and
     * {@link Typed} before falling back to basic type
     * 
     * @param o
     * @param var
     * @return Type resolved or {@code null}
     */
    public static Type resolveAt(Object o, TypeVariable<?> var) {
        Validate.notNull(var, "no variable to read");
        final GenericDeclaration genericDeclaration = var.getGenericDeclaration();
        if (genericDeclaration instanceof Class == false) {
            throw new IllegalArgumentException(TypeUtils.toLongString(var) + " is not declared by a Class");
        }
        return resolveAt(o, var, TypeUtils.getTypeArguments(o.getClass(), (Class<?>) genericDeclaration));
    }

    /**
     * Tries to "read" a {@link TypeVariable} from an object instance, taking into account {@link BindTypeVariable} and
     * {@link Typed} before falling back to basic type.
     * 
     * @param o
     * @param var
     * @param variablesMap prepopulated map for efficiency
     * @return Type resolved or {@code null}
     */
    public static Type resolveAt(Object o, TypeVariable<?> var, Map<TypeVariable<?>, Type> variablesMap) {
        final Class<?> rt = Validate.notNull(o, "null target").getClass();
        Validate.notNull(var, "no variable to read");
        final GenericDeclaration genericDeclaration = var.getGenericDeclaration();
        if (genericDeclaration instanceof Class == false) {
            throw new IllegalArgumentException(TypeUtils.toLongString(var) + " is not declared by a Class");
        }
        final Class<?> declaring = (Class<?>) genericDeclaration;
        if (!declaring.isInstance(o)) {
            throw new IllegalArgumentException(TypeUtils.toLongString(var) + " does not belong to " + rt);
        }
        return unrollVariables(variablesMap, var, o);
    }

    private static Type unrollVariables(Map<TypeVariable<?>, Type> typeArguments, final Type type, final Object o) {
        if (typeArguments == null) {
            typeArguments = Collections.<TypeVariable<?>, Type> emptyMap();
        }
        if (TypeUtils.containsTypeVariables(type)) {
            if (type instanceof TypeVariable<?>) {
                for (Class<?> c : init(o.getClass())) {
                    final Map<TypeVariable<?>, Method> gettersForType = TYPED_GETTERS.get(c);
                    if (gettersForType != null && gettersForType.containsKey(type)) {
                        return readTyped(gettersForType.get(type), o);
                    }
                }
                return unrollVariables(typeArguments, typeArguments.get(type), o);
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
                    final Type unrolled = unrollVariables(parameterizedTypeArguments, args[i], o);
                    if (unrolled != null) {
                        args[i] = unrolled;
                    }
                }
                return TypeUtils.parameterizeWithOwner(p.getOwnerType(), (Class<?>) p.getRawType(), args);
            }
            if (type instanceof WildcardType) {
                final WildcardType wild = (WildcardType) type;
                return TypeUtils.wildcardType().withUpperBounds(unrollBounds(typeArguments, wild.getUpperBounds(), o))
                    .withLowerBounds(unrollBounds(typeArguments, wild.getLowerBounds(), o)).build();
            }
        }
        return type;
    }

    private static Type[] unrollBounds(final Map<TypeVariable<?>, Type> typeArguments, final Type[] bounds,
        final Object o) {
        Type[] result = bounds;
        int i = 0;
        for (; i < result.length; i++) {
            final Type unrolled = unrollVariables(typeArguments, result[i], o);
            if (unrolled == null) {
                result = ArrayUtils.remove(result, i--);
            } else {
                result[i] = unrolled;
            }
        }
        return result;
    }

    /**
     * Friendlier string formatting of types that appends brackets for array types.
     * 
     * @param type
     * @return String
     */
    public static String toString(Type type) {
        Validate.notNull(type);
        if (TypeUtils.isArrayType(type)) {
            return toString(TypeUtils.getArrayComponentType(type)) + "[]";
        }
        // provide TypeUtils impl until lang 3.4 release:
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

    private static Type readTyped(Method method, Object target) {
        try {
            setAccessibleWorkaround(method);
            final Typed<?> typed = (Typed<?>) method.invoke(target);
            Validate.validState(typed != null, "%s returned null", method);
            return typed.getType();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * XXX Default access superclass workaround
     * 
     * When a {@code public} class has a default access superclass with {@code public} members, these members are
     * accessible. Calling them from compiled code works fine. Unfortunately, on some JVMs, using reflection to invoke
     * these members seems to (wrongly) prevent access even when the modifier is {@code public}. Calling
     * {@code setAccessible(true)} solves the problem but will only work from sufficiently privileged code. Better
     * workarounds would be gratefully accepted.
     * 
     * @param o the AccessibleObject to set as accessible
     */
    // borrowed from Commons Lang MemberUtils
    static void setAccessibleWorkaround(final AccessibleObject o) {
        if (o == null || o.isAccessible()) {
            return;
        }
        final Member m = (Member) o;
        if (Modifier.isPublic(m.getModifiers()) && isPackageAccess(m.getDeclaringClass().getModifiers())) {
            try {
                o.setAccessible(true);
            } catch (final SecurityException e) { // NOPMD
                // ignore in favor of subsequent IllegalAccessException
            }
        }
    }

    /**
     * Returns whether a given set of modifiers implies package access.
     * 
     * @param modifiers to test
     * @return {@code true} unless {@code package}/{@code protected}/{@code private} modifier detected
     */
    // borrowed from Commons Lang MemberUtils
    static boolean isPackageAccess(final int modifiers) {
        return (modifiers & ACCESS_TEST) == 0;
    }

    private static Iterable<Class<?>> init(Class<?> c) {
        final Iterable<Class<?>> result = ClassUtils.hierarchy(c, ClassUtils.Interfaces.INCLUDE);
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

    /**
     * Format a {@link Class} as a {@link String}.
     * 
     * @param c {@code Class} to format
     * @return String
     * @since 3.2
     */
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

    /**
     * Format a {@link TypeVariable} as a {@link String}.
     * 
     * @param v {@code TypeVariable} to format
     * @return String
     * @since 3.2
     */
    private static String typeVariableToString(TypeVariable<?> v) {
        final StringBuilder buf = new StringBuilder(v.getName());
        final Type[] bounds = v.getBounds();
        if (bounds.length > 0 && !(bounds.length == 1 && Object.class.equals(bounds[0]))) {
            buf.append(" extends ");
            appendAllTo(buf, " & ", v.getBounds());
        }
        return buf.toString();
    }

    /**
     * Format a {@link ParameterizedType} as a {@link String}.
     * 
     * @param p {@code ParameterizedType} to format
     * @return String
     * @since 3.2
     */
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

    /**
     * Format a {@link WildcardType} as a {@link String}.
     * 
     * @param w {@code WildcardType} to format
     * @return String
     * @since 3.2
     */
    private static String wildcardTypeToString(WildcardType w) {
        final StringBuilder buf = new StringBuilder().append('?');
        final Type[] lowerBounds = w.getLowerBounds();
        final Type[] upperBounds = w.getUpperBounds();
        if (lowerBounds.length > 1 || lowerBounds.length == 1 && lowerBounds[0] != null) {
            appendAllTo(buf.append(" super "), " & ", lowerBounds);
        } else if (upperBounds.length > 1 || upperBounds.length == 1 && !Object.class.equals(upperBounds[0])) {
            appendAllTo(buf.append(" extends "), " & ", upperBounds);
        }
        return buf.toString();
    }

    /**
     * Format a {@link GenericArrayType} as a {@link String}.
     * 
     * @param g {@code GenericArrayType} to format
     * @return String
     * @since 3.2
     */
    private static String genericArrayTypeToString(GenericArrayType g) {
        return String.format("%s[]", toString(g.getGenericComponentType()));
    }

    /**
     * Append {@code types} to @{code buf} with separator {@code sep}.
     * 
     * @param buf destination
     * @param sep separator
     * @param types to append
     * @return {@code buf}
     * @since 3.2
     */
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
