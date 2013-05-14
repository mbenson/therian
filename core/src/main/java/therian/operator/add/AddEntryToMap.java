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
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Add;
import therian.operation.ImmutableCheck;
import therian.operator.immutablecheck.DefaultImmutableChecker;
import therian.util.Types;

/**
 *
 */
@SuppressWarnings("rawtypes")
@StandardOperator
@DependsOn(DefaultImmutableChecker.class)
public class AddEntryToMap implements Operator<Add<Map.Entry, Map>> {

    @SuppressWarnings("unchecked")
    @Override
    public boolean perform(TherianContext context, Add<Map.Entry, Map> add) {
        add.getTargetPosition().getValue()
            .put(add.getSourcePosition().getValue().getKey(), add.getSourcePosition().getValue().getValue());
        add.setResult(true);
        return true;
    }

    @Override
    public boolean supports(TherianContext context, Add<Map.Entry, Map> add) {
        // cannot add to immutable types
        if (context.eval(ImmutableCheck.of(add.getTargetPosition())).booleanValue()) {
            return false;
        }
        if (add.getSourcePosition().getValue() == null) {
            return false;
        }
        final Map<TypeVariable<?>, Type> targetArgs =
            TypeUtils.getTypeArguments(add.getTargetType().getType(), Map.class);

        final Type targetKeyType = Types.unrollVariables(targetArgs, Map.class.getTypeParameters()[0]);

        if (targetKeyType != null && !TypeUtils.isInstance(add.getSourcePosition().getValue().getKey(), targetKeyType)) {
            return false;
        }
        final Type targetValueType = Types.unrollVariables(targetArgs, Map.class.getTypeParameters()[1]);

        return targetValueType == null
            || TypeUtils.isInstance(add.getSourcePosition().getValue().getValue(), targetValueType);
    }
}
