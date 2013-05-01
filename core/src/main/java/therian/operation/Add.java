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

import org.apache.commons.lang3.ObjectUtils;

import therian.position.Position;

/**
 * "Add" transformation.
 *
 * @param <SOURCE>
 * @param <TARGET>
 */
public class Add<SOURCE, TARGET> extends Transform<SOURCE, TARGET, Boolean, Position.Readable<TARGET>> {
    private boolean result;

    protected Add(Position.Readable<SOURCE> sourcePosition, Position.Readable<TARGET> targetPosition) {
        super(sourcePosition, targetPosition);
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    @Override
    protected Boolean provideResult() {
        return Boolean.valueOf(result);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!obj.getClass().equals(getClass())) {
            return false;
        }
        Add<?, ?> other = (Add<?, ?>) obj;
        return ObjectUtils.equals(other.getSourcePosition(), getSourcePosition())
                && ObjectUtils.equals(other.getTargetPosition(), getTargetPosition());
    }

    @Override
    public int hashCode() {
        int result = 51 << 4;
        result |= getClass().hashCode();
        result <<= 4;
        result |= ObjectUtils.hashCode(getSourcePosition());
        result <<= 4;
        result |= ObjectUtils.hashCode(getTargetPosition());
        return result;
    }

    @Override
    public String toString() {
        return String.format("Add %s to %s", getClass().getSimpleName(), getSourcePosition(), getTargetPosition());
    }

    public static <SOURCE, TARGET> Add<SOURCE, TARGET> to(Position.Readable<TARGET> targetPosition,
        Position.Readable<SOURCE> sourcePosition) {
        return new Add<SOURCE, TARGET>(sourcePosition, targetPosition);
    }
}
