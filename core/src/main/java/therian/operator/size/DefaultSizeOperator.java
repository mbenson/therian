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
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Size;
import therian.operator.OptimisticOperatorBase;

/**
 * Default {@link Size} {@link Operator}. Handles arrays (of which those with primitive component types of course cannot
 * be generalized), returns {@code 0} for {@code null}, and 1 otherwise.
 */
@StandardOperator
public class DefaultSizeOperator extends OptimisticOperatorBase<Size<?>> {

    @Override
    public boolean perform(TherianContext context, Size<?> operation) {
        final Object value = operation.getPosition().getValue();
        final int result;
        if (value == null) {
            result = 0;
        } else if (value.getClass().isArray()) {
            result = Array.getLength(value);
        } else {
            result = 1;
        }
        operation.setResult(result);
        return true;
    }
}
