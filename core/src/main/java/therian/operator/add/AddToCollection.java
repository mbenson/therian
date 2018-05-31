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
import java.util.Collection;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Add;
import therian.operation.ImmutableCheck;
import therian.operator.OperatorBase;
import therian.operator.immutablecheck.DefaultImmutableChecker;

/**
 * Add an element to a {@link Collection}.
 */
@StandardOperator
@DependsOn(DefaultImmutableChecker.class)
public class AddToCollection extends OperatorBase<Add<?, ? extends Collection<?>>> {

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean perform(TherianContext context, Add<?, ? extends Collection<?>> add) {
        final boolean result =
            ((Collection) add.getTargetPosition().getValue()).add(add.getSourcePosition().getValue());
        add.setResult(result);
        return true;
    }

    @Override
    public boolean supports(TherianContext context, Add<?, ? extends Collection<?>> add) {
        // cannot add to immutable types
        if (context.evalSuccess(ImmutableCheck.of(add.getTargetPosition()))) {
            return false;
        }
        if (!TypeUtils.isAssignable(add.getTargetPosition().getType(), Collection.class)) {
            return false;
        }
        final Type targetElementType =
            TypeUtils.unrollVariables(TypeUtils.getTypeArguments(add.getTargetPosition().getType(), Collection.class),
                Collection.class.getTypeParameters()[0]);

        if (targetElementType == null) {
            // raw collection
            return true;
        }
        return TypeUtils.isAssignable(add.getSourcePosition().getType(), targetElementType);
    }
}
