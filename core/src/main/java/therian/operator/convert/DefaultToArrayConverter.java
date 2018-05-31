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

import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Convert;
import therian.operation.GetElementType;
import therian.operator.getelementtype.GetArrayElementType;
import therian.position.Position;
import therian.util.Positions;

/**
 * Converts from source to {@link List} of target element type to target.
 */
@StandardOperator
@DependsOn({ GetArrayElementType.class, CollectionToArray.class })
public class DefaultToArrayConverter extends Converter.WithDynamicTarget<Object> {

    @Override
    public boolean perform(TherianContext context, final Convert<?, ?> convert) {
        final Type targetElementType = context.eval(GetElementType.of(convert.getTargetPosition()));
        final Position.ReadWrite<List<?>> list =
            Positions.readWrite(TypeUtils.parameterize(List.class, targetElementType));

        return context.evalSuccess(Convert.to(list, convert.getSourcePosition()))
            && context.evalSuccess(Convert.to(convert.getTargetPosition(), list));
    }

    @Override
    public boolean supports(TherianContext context, Convert<?, ?> convert) {
        if (!(super.supports(context, convert) && TypeUtils.isArrayType(convert.getTargetPosition().getType()))) {
            return false;
        }
        final GetElementType<?> getTargetElementType = GetElementType.of(convert.getTargetPosition());
        if (!context.supports(getTargetElementType)) {
            return false;
        }
        final Type targetElementType = context.eval(getTargetElementType);
        return context.supports(Convert.to(Positions.readWrite(TypeUtils.parameterize(List.class, targetElementType)),
            convert.getSourcePosition()));
    }
}
