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
package net.morph;

/**
 * Some operation; note that these are not intended for use on multiple threads.
 * An operation may not require any operator's assistance, as in the case of a
 * conversion to a type already assignable from the source.
 * 
 * @param <RESULT>
 */
public abstract class Operation<RESULT> {
    private boolean successful;

    /**
     * Get the result. Default implementation throws {@link OperationException}
     * if the operation was unsuccessful, then defers to
     * {@link #provideResult()}.
     * 
     * @return RESULT
     * @see #provideResult()
     */
    public RESULT getResult() {
        if (!isSuccessful()) {
            throw new OperationException(this, "result unavailable");
        }
        return provideResult();
    }

    final void invokeInit() {
        init();
    }

    /**
     * Initialize the operation with an available {@link MorphContext}.
     */
    protected void init() {
    }

    /**
     * Template method; must be implemented unless {@link #getResult()} is
     * overridden.
     * 
     * @return RESULT
     */
    protected RESULT provideResult() {
        throw new OperationException(this, "no result provided");
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}
