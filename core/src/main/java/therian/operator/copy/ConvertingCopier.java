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
package therian.operator.copy;

import therian.Operator;
import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Convert;
import therian.operation.Copy;
import therian.operation.ImmutableCheck;
import therian.operator.immutablecheck.DefaultImmutableChecker;
import therian.position.Position;
import therian.util.Positions;

/**
 * {@link Copy} {@link Operator} that attempts overwriting conversion for writable target positions storing immutable
 * values.
 */
@StandardOperator
@DependsOn(DefaultImmutableChecker.class)
public class ConvertingCopier extends Copier<Object, Object> {

    @Override
    public boolean perform(TherianContext context, Copy<?, ?> operation) {
        return context.forwardTo(Convert.to((Position.Writable<?>) operation.getTargetPosition(),
            operation.getSourcePosition()));
    }

    @Override
    public boolean supports(TherianContext context, Copy<?, ?> copy) {
        return Positions.isWritable(copy.getTargetPosition())
            && context.eval(ImmutableCheck.of(copy.getTargetPosition())).booleanValue()
            && context.supports(Convert.to((Position.Writable<?>) copy.getTargetPosition(), copy.getSourcePosition()));
    }

}
