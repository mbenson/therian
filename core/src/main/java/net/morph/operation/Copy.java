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

import net.morph.position.Position;
import net.morph.position.Position.Readable;

/**
 * Copy operation.
 * 
 * @param <SOURCE>
 * @param <TARGET>
 */
public class Copy<SOURCE, TARGET> extends Transform<SOURCE, TARGET, Void, Position.Readable<TARGET>> {

    private Copy(Readable<SOURCE> sourcePosition, Readable<TARGET> targetPosition) {
        super(sourcePosition, targetPosition);
    }

    @Override
    protected Void provideResult() {
        return null;
    }

    public static <S, T> Copy<S, T> to(Position.Readable<T> targetPosition, Position.Readable<S> sourcePosition) {
        return new Copy<S, T>(sourcePosition, targetPosition);
    }
}
