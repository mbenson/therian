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

import therian.Operator;
import therian.TherianContext;
import therian.Operator.DependsOn;
import therian.buildweaver.StandardOperator;
import therian.operation.Size;
import therian.operator.OptimisticOperatorBase;
import therian.util.Positions;

/**
 * {@link Operator} to take the size of an {@link Iterable}.
 */
@StandardOperator
@DependsOn(SizeOfIterator.class)
public class SizeOfIterable extends OptimisticOperatorBase<Size<Iterable<?>>> {

    @Override
    public boolean perform(TherianContext context, final Size<Iterable<?>> operation) {
        final Iterable<?> value = operation.getPosition().getValue();
        if (value == null) {
            operation.setResult(0);
        } else {
            operation.setResult(context.eval(Size.of(Positions.readOnly(value.iterator()))));
        }
        return true;
    }
}
