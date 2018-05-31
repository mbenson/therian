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

import therian.position.Position;

/**
 * "Add" transformation. Like {@link Collection#add(Object)}, the result of an {@link Add} operation is intended to
 * reflect whether a change was made to the target position.
 *
 * @param <SOURCE>
 * @param <TARGET>
 */
public class Add<SOURCE, TARGET> extends Transform<SOURCE, TARGET, Boolean, Position.Readable<TARGET>> {

    /**
     * Fluent factory method.
     * @param targetPosition
     * @param sourcePosition
     * @return {@link Add}
     */
    public static <SOURCE, TARGET> Add<SOURCE, TARGET> to(Position.Readable<TARGET> targetPosition,
        Position.Readable<SOURCE> sourcePosition) {
        return new Add<>(sourcePosition, targetPosition);
    }

    protected Add(Position.Readable<SOURCE> sourcePosition, Position.Readable<TARGET> targetPosition) {
        super(sourcePosition, targetPosition);
    }

    public void setResult(boolean result) {
        setResult(Boolean.valueOf(result));
    }
}
