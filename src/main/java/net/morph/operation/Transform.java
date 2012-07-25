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

import net.morph.Operation;
import net.morph.position.Position;

/**
 * Abstract transform operation.
 * A "transformer" is an operator over a transform operation.  Defining "Transformer" in terms of
 * our object model would constrict the behavior of transformer implementations in detrimental ways.
 */
public abstract class Transform<SOURCE, TARGET, RESULT, TARGET_POSITION extends Position<TARGET>> extends Operation<RESULT> {
    private final Position.Readable<SOURCE> sourcePosition;
    private final TARGET_POSITION targetPosition;

    /**
     * Create a new Transform instance.
     * @param sourcePosition
     * @param targetPosition
     */
    protected Transform(Position.Readable<SOURCE> sourcePosition, TARGET_POSITION targetPosition) {
        super();
        this.sourcePosition = sourcePosition;
        this.targetPosition = targetPosition;
    }

    /**
     * Get the sourcePosition.
     * @return Position.Readable<SOURCE>
     */
    public Position.Readable<SOURCE> getSourcePosition() {
        return sourcePosition;
    }

    /**
     * Get the targetPosition.
     * @return TARGET_POSITION
     */
    public TARGET_POSITION getTargetPosition() {
        return targetPosition;
    }

    @Override
    public final RESULT getResult() {
        return super.getResult();
    }
}
