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

import java.util.Collection;

import org.apache.commons.lang3.ObjectUtils;

import therian.position.Position;

/**
 * "Add all" transformation. Like {@link Collection#addAll(Collection)} result of an {@link AddAll} operation is
 * intended to reflect whether a change was made to the target position.
 *
 * @param <SOURCE>
 * @param <TARGET>
 */
public class AddAll<SOURCE, TARGET> extends Transform<SOURCE, TARGET, Boolean, Position.Readable<TARGET>> {
    private boolean result;

    protected AddAll(Position.Readable<SOURCE> sourcePosition, Position.Readable<TARGET> targetPosition) {
        super(sourcePosition, targetPosition);
    }

    public void setResult(boolean result) {
        setResult(Boolean.valueOf(result));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!obj.getClass().equals(getClass())) {
            return false;
        }
        AddAll<?, ?> other = (AddAll<?, ?>) obj;
        return ObjectUtils.equals(other.getSourcePosition(), getSourcePosition())
            && ObjectUtils.equals(other.getTargetPosition(), getTargetPosition());
    }

    @Override
    public int hashCode() {
        int result = 47 << 4;
        result |= getClass().hashCode();
        result <<= 4;
        result |= ObjectUtils.hashCode(getSourcePosition());
        result <<= 4;
        result |= ObjectUtils.hashCode(getTargetPosition());
        return result;
    }

    @Override
    public String toString() {
        return String.format("Add all %s to %s", getClass().getSimpleName(), getSourcePosition(), getTargetPosition());
    }

    public static <SOURCE, TARGET> AddAll<SOURCE, TARGET> to(Position.Readable<TARGET> targetPosition,
        Position.Readable<SOURCE> sourcePosition) {
        return new AddAll<SOURCE, TARGET>(sourcePosition, targetPosition);
    }

}
