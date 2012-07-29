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
package net.morph.operator;

import net.morph.MorphContext;
import net.morph.Operator;
import net.morph.operation.Convert;
import net.morph.operation.Copy;
import net.morph.operation.ImmutableCheck;
import net.morph.position.Position;

/**
 * {@link Copy} {@link Operator} that attempts overwriting conversion for writable target positions storing immutable
 * values.
 */
public class ConvertingCopier implements Operator<Copy<?, ?>> {

    public void perform(Copy<?, ?> operation) {
        // TODO create convert operation
        Convert<?, ?> convert = null;
        MorphContext.getRequiredInstance().forwardTo(convert);
    }

    public boolean supports(Copy<?, ?> operation) {
        return operation.getTargetPosition() instanceof Position.Writable
            && MorphContext.getRequiredInstance().eval(ImmutableCheck.of(operation.getTargetPosition())).booleanValue();
    }
}
