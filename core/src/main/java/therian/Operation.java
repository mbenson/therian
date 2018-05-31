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

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.behavior.Caching;
import therian.util.Types;

/**
 * Some operation; note that these are not intended for use on multiple threads. A concrete {@link Operation} class
 * should have its {@code RESULT} type parameter fully bound.
 *
 * @param <RESULT> result type
 */
public abstract class Operation<RESULT> {
    /**
     * {@link Operation} profile used for caching.
     * 
     * @see Caching#ALL
     */
    public static class Profile {
        private final Type genericType;
        private final Object[] discriminator;
        private final int hashCode;

        public Profile(Type genericType, Object... discriminator) {
            super();
            this.genericType = Validate.notNull(genericType);
            this.discriminator = discriminator;
            hashCode = ArrayUtils.isEmpty(discriminator) ? Objects.hash(genericType)
                : Arrays.deepHashCode(ArrayUtils.insert(0, discriminator, genericType));
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Profile)) {
                return false;
            }
            final Profile other = (Profile) obj;
            return Objects.equals(genericType, other.genericType) && Arrays.equals(discriminator, other.discriminator);
        }

        @Override
        public String toString() {
            return String.format("%s: %s/%s", Types.getSimpleName(getClass()), genericType,
                Arrays.toString(discriminator));
        }
    }

    private static final TypeVariable<?> TYPE_VARIABLE_RESULT = Operation.class.getTypeParameters()[0];

    private static final Map<Class<?>, Boolean> VALID_INFO = new HashMap<>();

    private static boolean init(Class<?> type) {
        final boolean valid;
        synchronized (type) {
            if (VALID_INFO.containsKey(type)) {
                valid = VALID_INFO.get(type).booleanValue();
            } else if (Modifier.isAbstract(type.getModifiers())) {
                valid = true;
            } else {
                final Type resultType =
                    TypeUtils.unrollVariables(TypeUtils.getTypeArguments(type, Operation.class), TYPE_VARIABLE_RESULT);
                valid = !TypeUtils.containsTypeVariables(resultType);
                Validate.isTrue(valid, "%s does not fully bind type parameter %s from %s", type,
                    TYPE_VARIABLE_RESULT.getName(), Operation.class);
                VALID_INFO.put(type, Boolean.valueOf(valid));
            }
        }
        final Class<?> parent = type.getSuperclass();
        if (!Operation.class.equals(parent)) {
            init(parent.asSubclass(Operation.class));
        }
        return valid;
    }

    {
        @SuppressWarnings("unchecked")
        final Class<? extends Operation<?>> c = (Class<? extends Operation<?>>) getClass();
        Validate.isTrue(init(c), "Invalid %s: %s", Operation.class.getName(), c);
    }

    private boolean successful;
    private RESULT result;
    private volatile Profile profile;

    /**
     * Get the result. Default implementation throws {@link OperationException} if the operation was unsuccessful.
     *
     * @return RESULT
     */
    public RESULT getResult() {
        if (!isSuccessful()) {
            throw new OperationException(this, "result unavailable");
        }
        return result;
    }

    /**
     * Set the result of this {@link Operation}.
     *
     * @param result to set
     */
    public void setResult(RESULT result) {
        this.result = result;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    /**
     * Learn whether {@code operator} seems to implement {@code this}.
     *
     * @param operator to check
     * @return boolean
     */
    public boolean matches(Operator<?> operator) {
        final Type expectedType = TypeUtils.unrollVariables(
            TypeUtils.getTypeArguments(operator.getClass(), Operator.class), Operator.class.getTypeParameters()[0]);

        if (!TypeUtils.isInstance(this, expectedType)) {
            return false;
        }
        final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(expectedType, Operation.class);
        for (Class<?> c : ClassUtils.hierarchy(TypeUtils.getRawType(expectedType, operator.getClass()))) {
            if (c.equals(Operation.class)) {
                break;
            }
            for (TypeVariable<?> var : c.getTypeParameters()) {
                Type type = Types.resolveAt(this, var, typeArguments);
                if (type == null || typeArguments == null) {
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
        return true;
    }

    /**
     * Create the {@link Profile} for this {@link Operation}.
     * 
     * @param genericType
     * @return Profile
     */
    protected Profile createProfile(Type genericType) {
        return new Profile(genericType);
    }

    final Profile getProfile() {
        if (profile == null) {
            synchronized (this) {
                if (profile == null) {
                    profile = createProfile(getGenericType());
                }
            }
        }
        return profile;
    }

    /**
     * Get the "generic type" of this {@link Operation}, which parameterizes the narrowest type with the runtime
     * bindings of its declared type variables, if any.
     * 
     * @return Type
     * @see Types#resolveAt(Object, TypeVariable)
     */
    private final Type getGenericType() {
        final Class<?> raw = getClass();
        final TypeVariable<?>[] typeParameters = raw.getTypeParameters();
        if (ArrayUtils.isEmpty(typeParameters)) {
            return raw;
        }
        final Type[] parameters = new Type[typeParameters.length];

        // use empty type variable map because we're relying on @BindTypeVariable functionality:
        final Map<TypeVariable<?>, Type> typeVariableMap = Collections.emptyMap();
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = Types.resolveAt(this, typeParameters[i], typeVariableMap);
        }
        return TypeUtils.parameterize(raw, parameters);
    }

}
