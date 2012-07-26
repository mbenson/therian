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
import net.morph.position.Position.Readable;

/**
 * Operation to check an object for immutability. Uses success as its result,
 * because it wants to run until some {@link Operator} gives it a {@code true}
 * value.
 */
public final class ImmutableCheck<T> extends Operation<Boolean> {
    private final Position.Readable<T> position;

    private ImmutableCheck(Readable<T> position) {
        super();
        this.position = position;
    }

    public Position.Readable<T> getPosition() {
        return position;
    }

    /**
     * Success as result.
     */
    @Override
    public Boolean getResult() {
        return isSuccessful();
    }

    /**
     * Create an {@link ImmutableCheck} operation against {@code position}.
     * 
     * @param T
     * @param position
     * @return operation
     */
    public static <T> ImmutableCheck<T> of(Position.Readable<T> position) {
        return new ImmutableCheck<T>(position);
    }
}
