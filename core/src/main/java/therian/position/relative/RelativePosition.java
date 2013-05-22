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
package therian.position.relative;

import therian.position.Position;

/**
 * {@link Position} relative to some other {@link Position.Readable}.
 *
 * @param <P>
 * @param <T>
 */
public interface RelativePosition<P, T> extends Position<T> {

    /**
     * Readable {@link RelativePosition}.
     *
     * @param <P>
     * @param <T>
     */
    public interface Readable<P, T> extends RelativePosition<P, T>, Position.Readable<T> {
    }

    /**
     * Writable {@link RelativePosition}.
     *
     * @param <P>
     * @param <T>
     */
    public interface Writable<P, T> extends RelativePosition<P, T>, Position.Writable<T> {
    }

    /**
     * Read/write {@link RelativePosition}.
     *
     * @param <P>
     * @param <T>
     */
    public interface ReadWrite<P, T> extends RelativePosition<P, T>, Position.ReadWrite<T> {
    }

    Position.Readable<? extends P> getParentPosition();
}
