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

import java.util.Map;

import therian.Operator;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Size;

/**
 * {@link Operator} to take the size of a {@link Map}.
 */
@StandardOperator
public class SizeOfMap implements Operator<Size<Map<?, ?>>> {

    @Override
    public boolean perform(TherianContext context, Size<Map<?, ?>> operation) {
        final Map<?, ?> value = operation.getPosition().getValue();
        operation.setResult(value == null ? 0 : value.size());
        return true;
    }

    @Override
    public boolean supports(TherianContext context, Size<Map<?, ?>> operation) {
        return true;
    }
}
