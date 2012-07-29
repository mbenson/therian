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
 * Implements an {@link Operation}. Note that a concrete {@link Operator} implementation should host no direct type
 * variables.
 * 
 * @param <OPERATION>
 * @see Operators#validateImplementation(Operator)
 */
public interface Operator<OPERATION extends Operation<?>> {

    /**
     * Perform the specified operation.
     * 
     * @param operation
     *            to perform
     */
    void perform(OPERATION operation);

    /**
     * Learn whether an operation is supported.
     * 
     * @param operation
     *            to check
     * @return true if supported
     */
    boolean supports(OPERATION operation);
}
