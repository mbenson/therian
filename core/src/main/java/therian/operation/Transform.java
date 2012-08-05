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
import org.apache.commons.lang3.Validate;

import therian.Operation;
import therian.position.Position;


/**
 * Abstract transform operation. A "transformer" is an operator over a transform operation. Defining "Transformer" in
 * terms of our object model would constrict the behavior of transformer implementations in detrimental ways.
 */
public abstract class Transform<SOURCE, TARGET, RESULT, TARGET_POSITION extends Position<TARGET>> extends
    Operation<RESULT> {
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
     * Get the sourcePosition.
     * 
     * @return Position.Readable<SOURCE>
     */
    public Position.Readable<SOURCE> getSourcePosition() {
        return sourcePosition;
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
        return String.format("%s %s to %s", getClass().getSimpleName(), getSourcePosition(), getTargetPosition());
    }
}
