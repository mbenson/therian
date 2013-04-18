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
package therian;

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
     * @param context active
     * @param operation to perform
     */
    // TODO return boolean; this way no operator can forget to set success and the framework can set it on the operation
    void perform(TherianContext context, OPERATION operation);

    /**
     * Learn whether an operation is supported. This check can be fairly perfunctory as the evaluation of a given
     * {@link Operation} sets an associated success status, thus just because an {@link Operation} is deemed to be
     * "supported" does not <em>guarantee</em> it will be successfully evaluated.
     * 
     * @param context active
     * @param operation to check
     * 
     * @return true if supported
     */
    boolean supports(TherianContext context, OPERATION operation);
}
