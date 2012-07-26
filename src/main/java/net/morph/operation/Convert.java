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
package net.morph.operation;

import java.lang.reflect.Type;

import net.morph.MorphContext;
import net.morph.position.Position;
import net.morph.position.Position.Readable;
import net.morph.position.Position.Writable;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;

/**
 * Convert operation. An attempt to convert an immutable value to an assignable
 * type automatically succeeds.
 * 
 * @param <SOURCE>
 * @param <TARGET>
 */
public class Convert<SOURCE, TARGET> extends Transform<SOURCE, TARGET, TARGET, Position.Writable<TARGET>> {
    private class Result implements Position.Writable<TARGET> {
        TARGET value;

        public Type getType() {
            return Convert.super.getTargetPosition().getType();
        }

        public void setValue(final TARGET value) {
            final Type type = getType();
            Validate.isTrue(!(value == null && isPrimitive(type)), "Illegal value %s for type %s", value, type);
            this.value = value;
            Convert.super.getTargetPosition().setValue(value);
        }

        @Override
        public String toString() {
            return Convert.super.getTargetPosition().toString();
        }

        private boolean isPrimitive(Type type) {
            return type instanceof Class && ((Class<?>) type).isPrimitive();
        }
    }

    private final Result result;

    private Convert(Readable<SOURCE> sourcePosition, Writable<TARGET> targetPosition) {
        super(sourcePosition, targetPosition);
        this.result = new Result();
    }

    @Deprecated
    // TODO make private, replace with fluent API
    public Convert(Readable<SOURCE> sourcePosition, final Type targetType) {
        this(sourcePosition, new Position.Writable<TARGET>() {
            {
                Validate.notNull(targetType, "targetType");
            }

            public Type getType() {
                return targetType;
            }

            public void setValue(TARGET value) {
            }

            @Override
            public String toString() {
                return targetType.toString();
            }
        });
    }

    @Override
    protected void init() {
        super.init();
        if (TypeUtils.isAssignable(getSourcePosition().getType(), super.getTargetPosition().getType())) {
            if (MorphContext.getRequiredInstance().eval(ImmutableCheck.of(getSourcePosition())).booleanValue()) {
                @SuppressWarnings("unchecked")
                final TARGET value = (TARGET) getSourcePosition().getValue();
                getTargetPosition().setValue(value);
                setSuccessful(true);
            }
        }
    }

    @Override
    protected TARGET provideResult() {
        return result.value;
    }

    @Override
    public Position.Writable<TARGET> getTargetPosition() {
        return result;
    }
}
