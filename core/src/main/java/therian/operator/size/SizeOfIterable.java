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

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Size;
import therian.position.Ref;

/**
 * {@link Operator} to take the size of an {@link Iterable}.
 */
@StandardOperator
public class SizeOfIterable implements Operator<Size<Iterable<?>>> {

    @Override
    public boolean perform(TherianContext context, final Size<Iterable<?>> operation) {
        final Iterable<?> value = operation.getPosition().getValue();
        if (value == null) {
            operation.setResult(0);
        } else {
            operation.setResult(context.eval(Size.of(Ref.to(value.iterator()))));
        }
        return true;
    }

    @Override
    public boolean supports(TherianContext context, Size<Iterable<?>> operation) {
        return TypeUtils.isAssignable(operation.getPosition().getType(), Iterable.class);
    }

}
