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
package therian.operator;

import therian.Operator;
import therian.Therian;
import therian.TherianContext;
import therian.operation.Convert;
import therian.operation.Copy;
import therian.operation.ImmutableCheck;
import therian.position.Position;

/**
 * {@link Copy} {@link Operator} that attempts overwriting conversion for writable target positions storing immutable
 * values.
 */
public class ConvertingCopier implements Operator<Copy<?, ?>> {

    public void perform(Copy<?, ?> operation) {
        final Convert<?, ?> convert =
            Convert.to((Position.Writable<?>) operation.getTargetPosition(), operation.getSourcePosition());
        TherianContext.getRequiredInstance().forwardTo(convert);
    }

    public boolean supports(Copy<?, ?> copy) {
        if (copy.getTargetPosition() instanceof Position.Writable) {
            final TherianContext context = TherianContext.getInstance();
            return context.eval(ImmutableCheck.of(copy.getTargetPosition())).booleanValue()
                && context.getTypedContext(Therian.class).supports(
                    Convert.to((Position.Writable<?>) copy.getTargetPosition(), copy.getSourcePosition()));
        }
        return false;
    }
}
