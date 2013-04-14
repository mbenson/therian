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
package therian.operator.size;

import java.lang.reflect.Array;

import therian.Operator;
import therian.operation.Size;

/**
 * Default {@link Size} {@link Operator}. Handles arrays (of which those with primitive component types of course cannot
 * be generalized), returns {@code 0} for {@code null}, and 1 otherwise.
 */
public class DefaultSizeOperator implements Operator<Size<?>> {

    public void perform(Size<?> operation) {
        final Object value = operation.getPosition().getValue();
        final int result = value == null ? 0 : value.getClass().isArray() ? Array.getLength(value) : 1;
        operation.setResult(result);
        operation.setSuccessful(true);
    }

    public boolean supports(Size<?> operation) {
        return true;
    }

}
