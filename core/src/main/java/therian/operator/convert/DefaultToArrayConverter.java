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
import java.util.Collection;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.TherianContext;
import therian.Operator.DependsOn;
import therian.buildweaver.StandardOperator;
import therian.operation.Convert;
import therian.operation.GetElementType;
import therian.operator.getelementtype.GetArrayElementType;
import therian.position.Position;
import therian.util.Positions;

/**
 * Converts from source to {@link Collection} to target.
 */
@StandardOperator
@DependsOn({ GetArrayElementType.class, CollectionToArray.class })
public class DefaultToArrayConverter extends Converter.WithDynamicTarget<Object> {

    @Override
    public boolean perform(TherianContext context, final Convert<?, ?> convert) {
        @SuppressWarnings("rawtypes")
        final Position.ReadWrite<Collection> coll = Positions.readWrite(Collection.class);
        if (!context.evalSuccess(Convert.to(coll, convert.getSourcePosition()))) {
            return false;
        }
        return context.forwardTo(Convert.to(convert.getTargetPosition(), coll));
    }

    @Override
    public boolean supports(TherianContext context, Convert<?, ?> convert) {
        if (!(super.supports(context, convert) && TypeUtils.isArrayType(convert.getTargetPosition().getType()))) {
            return false;
        }
        if (!context.supports(Convert.to(Collection.class, convert.getSourcePosition()))) {
            return false;
        }
        final GetElementType<?> getTargetElementType = GetElementType.of(convert.getTargetPosition());
        if (!context.supports(getTargetElementType)) {
            return false;
        }
        final Type targetElementType = context.eval(getTargetElementType);
        final GetElementType<?> getSourceElementType = GetElementType.of(convert.getSourcePosition());
        if (!context.supports(getSourceElementType)) {
            return false;
        }
        final Type sourceElementType = context.eval(getSourceElementType);
        return TypeUtils.isAssignable(sourceElementType, targetElementType);
    }

}
