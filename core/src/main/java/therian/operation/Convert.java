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

import java.lang.reflect.Type;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.position.AbstractPosition;
import therian.position.Position;

/**
 * Convert operation.
 * 
 * @param <SOURCE>
 * @param <TARGET>
 */
public class Convert<SOURCE, TARGET> extends Transform<SOURCE, TARGET, TARGET, Position.Writable<TARGET>> {
    private class Result extends AbstractPosition.Writable<TARGET> {
        TARGET value;

        @Override
        public Type getType() {
            return getTarget().getType();
        }

        @Override
        public void setValue(final TARGET value) {
            if (value == null) {
                final Type type = getType();
                Validate.isTrue(!isPrimitive(type), "Null value illegal for type %s", value, type);
            }
            this.value = value;
            getTarget().setValue(value);
        }

        @Override
        public String toString() {
            return getTarget().toString();
        }

        private boolean isPrimitive(Type type) {
            return type instanceof Class && ((Class<?>) type).isPrimitive();
        }

        private Position.Writable<TARGET> getTarget() {
            return Convert.super.getTargetPosition();
        }

    }

    private final Result result;

    protected Convert(Position.Readable<SOURCE> sourcePosition, Position.Writable<TARGET> targetPosition) {
        super(sourcePosition, targetPosition);
        this.result = new Result();
    }

    private Convert(Position.Readable<SOURCE> sourcePosition, final Type targetType) {
        this(sourcePosition, new Position.Writable<TARGET>() {
            {
                Validate.notNull(targetType, "targetType");
            }

            @Override
            public Type getType() {
                return targetType;
            }

            @Override
            public void setValue(TARGET value) {
            }

            @Override
            public String toString() {
                return TypeUtils.toString(targetType);
            }
        });
    }

    @Override
    protected TARGET provideResult() {
        return result.value;
    }

    @Override
    public Position.Writable<TARGET> getTargetPosition() {
        return result;
    }

    public static <S, T> Convert<S, T> to(Class<T> targetType, Position.Readable<S> sourcePosition) {
        return new Convert<S, T>(sourcePosition, targetType);
    }

    public static <S, T> Convert<S, T> to(org.apache.commons.lang3.reflect.TypeLiteral<T> targetType,
        Position.Readable<S> sourcePosition) {
        return new Convert<S, T>(sourcePosition, targetType.value);
    }

    public static <S, T> Convert<S, T> to(Position.Writable<T> targetPosition, Position.Readable<S> sourcePosition) {
        return new Convert<S, T>(sourcePosition, targetPosition);
    }
}
