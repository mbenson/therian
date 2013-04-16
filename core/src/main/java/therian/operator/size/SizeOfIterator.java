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

import java.util.Iterator;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operation;
import therian.Operator;
import therian.TherianContext;
import therian.operation.Size;

/**
 * {@link Operator} to take the size of an {@link Iterator}. Note that taking the size of an {@link Iterator} is
 * destructive in that it exhausts the {@link Iterator}. So the {@link Operation} itself should be used sparingly and
 * with care.
 */
public class SizeOfIterator implements Operator<Size<Iterator<?>>> {

    public void perform(TherianContext context, Size<Iterator<?>> operation) {
        final Iterator<? extends Object> value = operation.getPosition().getValue();
        int result = 0;
        for (; value != null && value.hasNext(); result++) {
            value.next();
        }
        operation.setResult(result);
        operation.setSuccessful(true);
    }

    public boolean supports(TherianContext context, Size<Iterator<?>> operation) {
        return TypeUtils.isAssignable(operation.getPosition().getType(), Iterator.class);
    }

}
