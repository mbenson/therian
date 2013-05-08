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
package therian.operation;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.BindTypeVariable;
import therian.Operation;
import therian.Typed;
import therian.position.Position;
import therian.util.Types;

/**
 * Abstract transform operation. A "transformer" is an operator over a transform operation. Defining "Transformer" in
 * terms of our object model would constrict the behavior of transformer implementations in detrimental ways.
 */
public abstract class Transform<SOURCE, TARGET, RESULT, TARGET_POSITION extends Position<TARGET>> extends
    Operation<RESULT> {

    private static Type narrow(Position.Readable<?> pos) {
        if (pos.getValue() != null) {
            final Type type = pos.getType();
            if (!pos.getValue().getClass().equals(TypeUtils.getRawType(type, null))) {
                final Class<?> rawValueType = pos.getValue().getClass();
                final TypeVariable<?>[] typeParameters = rawValueType.getTypeParameters();
                final Type result;
                if (typeParameters.length > 0 && type instanceof ParameterizedType) {
                    final Map<TypeVariable<?>, Type> argMappings =
                        TypeUtils.determineTypeArguments(rawValueType, (ParameterizedType) type);
                    final Type[] args = new Type[typeParameters.length];

                    int index = 0;
                    for (TypeVariable<?> typeVariable : typeParameters) {
                        args[index++] = ObjectUtils.defaultIfNull(argMappings.get(typeVariable), Types.WILDCARD_ALL);
                    }
                    result = Types.parameterize(rawValueType, args);
                } else {
                    result = rawValueType;
                }
                return result;
            }
        }
        return pos.getType();
    }

    private final Position.Readable<SOURCE> sourcePosition;
    private final TARGET_POSITION targetPosition;

    /**
     * Create a new Transform instance.
     *
     * @param sourcePosition
     * @param targetPosition
     */
    protected Transform(Position.Readable<SOURCE> sourcePosition, TARGET_POSITION targetPosition) {
        super();
        this.sourcePosition = Validate.notNull(sourcePosition, "sourcePosition");
        this.targetPosition = Validate.notNull(targetPosition, "targetPosition");
    }

    /**
     * Get the narrowest possible source type, deduced from source position type/value.
     *
     * @return Typed
     */
    @BindTypeVariable
    public Typed<SOURCE> getSourceType() {
        final Position.Readable<SOURCE> source = getSourcePosition();
        final Type result = narrow(source);
        if (!Types.equals(result, source.getType())) {
            return new Typed<SOURCE>() {
                @Override
                public Type getType() {
                    return result;
                }
            };
        }
        return source;
    }

    /**
     * Get the sourcePosition.
     *
     * @return Position.Readable<SOURCE>
     */
    public Position.Readable<SOURCE> getSourcePosition() {
        return sourcePosition;
    }

    /**
     * Get the narrowest possible target type. If this {@link Transform} operation maps its {@code TARGET_POSITION} type
     * parameter as some {@link Readable} then this will be deduced from target position type/value, else the target
     * position will be returned.
     *
     * @return Typed
     */
    @BindTypeVariable
    public Typed<TARGET> getTargetType() {
        final TARGET_POSITION target = getTargetPosition();
        if (TypeUtils.isAssignable(
            TypeUtils.getTypeArguments(getClass(), Transform.class).get(Transform.class.getTypeParameters()[3]),
            Position.Readable.class)) {
            final Type result = narrow((Position.Readable<TARGET>) target);
            if (!Types.equals(result, target.getType())) {
                return new Typed<TARGET>() {

                    @Override
                    public Type getType() {
                        return result;
                    }
                };
            }
        }
        return target;
    }

    /**
     * Get the targetPosition.
     *
     * @return TARGET_POSITION
     */
    public TARGET_POSITION getTargetPosition() {
        return targetPosition;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!obj.getClass().equals(getClass())) {
            return false;
        }
        Transform<?, ?, ?, ?> other = (Transform<?, ?, ?, ?>) obj;
        return ObjectUtils.equals(other.getSourcePosition(), getSourcePosition())
            && ObjectUtils.equals(other.getTargetPosition(), getTargetPosition());
    }

    @Override
    public int hashCode() {
        int result = 41 << 4;
        result |= getClass().hashCode();
        result <<= 4;
        result |= ObjectUtils.hashCode(getSourcePosition());
        result <<= 4;
        result |= ObjectUtils.hashCode(getTargetPosition());
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s%s to %s", getSimpleName(), getSourcePosition(), getTargetPosition());
    }

    private String getSimpleName() {
        final StringBuilder buf = new StringBuilder();
        Class<?> c = getClass();
        while (c != null) {
            buf.insert(0, ' ').insert(0, c.getSimpleName());
            c = c.getEnclosingClass();
        }
        return buf.toString();
    }
}
