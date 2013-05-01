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
package therian.operator.add;

import java.lang.reflect.Type;
import java.util.ListIterator;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Add;
import therian.operation.ImmutableCheck;
import therian.util.Types;

/**
 * Add an element to a {@link ListIterator}.
 */
@StandardOperator
public class AddToListIterator implements Operator<Add<?, ListIterator<?>>> {

    @Override
    @SuppressWarnings("unchecked")
    public boolean perform(TherianContext context, Add<?, ListIterator<?>> operation) {
        @SuppressWarnings("rawtypes")
        final ListIterator listIterator = operation.getTargetPosition().getValue();

        int mark = 0;
        while (listIterator.hasNext()) {
            listIterator.next();
            mark++;
        }
        try {
            listIterator.add(operation.getSourcePosition().getValue());
            operation.setResult(Boolean.TRUE);
            return true;
        } catch (UnsupportedOperationException e) {
            return false;
        } finally {
            for (; mark >= 0; mark--) {
                listIterator.previous();
            }
        }
    }

    @Override
    public boolean supports(TherianContext context, Add<?, ListIterator<?>> operation) {
        // cannot add to immutable types
        if (context.eval(ImmutableCheck.of(operation.getTargetPosition())).booleanValue()) {
            return false;
        }
        if (!TypeUtils.isAssignable(operation.getTargetPosition().getType(), ListIterator.class)) {
            return false;
        }
        final Type targetElementType =
                Types.unrollVariables(
                    TypeUtils.getTypeArguments(operation.getTargetPosition().getType(), ListIterator.class),
                    ListIterator.class.getTypeParameters()[0]);

        if (targetElementType == null) {
            // raw
            return true;
        }
        return TypeUtils.isAssignable(operation.getSourcePosition().getType(), targetElementType);
    }

}
