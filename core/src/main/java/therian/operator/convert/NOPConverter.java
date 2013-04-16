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
package therian.operator.convert;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.TherianContext;
import therian.operation.Convert;
import therian.operation.ImmutableCheck;
import therian.position.Position;

/**
 * Uses source value as target value when assignable and immutable.
 */
public class NOPConverter implements therian.Operator<Convert<?, ?>> {

    public void perform(TherianContext context, Convert<?, ?> operation) {
        // silly anal ways to avoid suppressing warnings on the whole method:
        @SuppressWarnings("rawtypes")
        final Convert raw = operation;
        @SuppressWarnings({ "unused", "unchecked" })
        final Void dummy = dumpTo(raw.getTargetPosition(), raw.getSourcePosition());
        operation.setSuccessful(true);
    }

    private <T> Void dumpTo(Position.Writable<? super T> target, Position.Readable<? extends T> source) {
        target.setValue(source.getValue());
        return null;
    }

    public boolean supports(TherianContext context, Convert<?, ?> operation) {
        return TypeUtils.isAssignable(operation.getSourcePosition().getType(), operation.getTargetPosition().getType())
            && context.eval(ImmutableCheck.of(operation.getSourcePosition())).booleanValue();
    }

}
