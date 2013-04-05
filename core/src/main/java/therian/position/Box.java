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
package therian.position;

import java.lang.reflect.Type;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;

/**
 * Portable read/write {@link Position}.
 *
 * @param <T>
 */
public class Box<T> implements Position.ReadWrite<T> {
    private final Type type;
    private T value;

    public Box(Type type) {
        this(type, null);
    }

    public Box(Type type, T value) {
        super();
        this.type = Validate.notNull(type, "type");
        Validate.isTrue(TypeUtils.isInstance(value, type), "%s is not an instance of %s", value, type);
        this.value = value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Box == false) {
            return false;
        }
        Box<?> other = (Box<?>) obj;
        return other.getType().equals(type) && ObjectUtils.equals(other.getValue(), value);
    }

    @Override
    public int hashCode() {
        int result = 43 << 4;
        result |= type.hashCode();
        result <<= 4;
        result |= ObjectUtils.hashCode(value);
        return result;
    }

    @Override
    public String toString() {
        return String.format("Box<%s>(%s)", type, value);
    }

}
