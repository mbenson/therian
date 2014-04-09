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

import therian.OperationException;
import therian.position.Position;
import therian.position.Position.Readable;

/**
 * Copy operation.
 *
 * @param <SOURCE>
 * @param <TARGET>
 */
public class Copy<SOURCE, TARGET> extends Transform<SOURCE, TARGET, Void, Position.Readable<TARGET>> {
    /**
     * "Copy safely" operation.
     *
     * @param <SOURCE>
     * @param <TARGET>
     */
    public static class Safely<SOURCE, TARGET> extends Copy<SOURCE, TARGET> {

        private Safely(Readable<SOURCE> sourcePosition, Readable<TARGET> targetPosition) {
            super(sourcePosition, targetPosition);
        }

        /**
         * Suppresses {@link OperationException} for failed operation.
         */
        @Override
        public Void getResult() {
            return null;
        }

        public static <S, T> Safely<S, T> to(Position.Readable<T> targetPosition, Position.Readable<S> sourcePosition) {
            return new Safely<S, T>(sourcePosition, targetPosition);
        }
    }

    protected Copy(Position.Readable<SOURCE> sourcePosition, Position.Readable<TARGET> targetPosition) {
        super(sourcePosition, targetPosition);
    }

    public static <S, T> Copy<S, T> to(Position.Readable<T> targetPosition, Position.Readable<S> sourcePosition) {
        return new Copy<S, T>(sourcePosition, targetPosition);
    }
}
