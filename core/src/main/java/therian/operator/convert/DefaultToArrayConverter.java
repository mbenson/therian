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
 * Converts assignable element containers to arrays:
 * <ul>
 * <li>uses {@link GetElementType} to determine element type (if unavailable for source type, singleton is assumed)</li>
 * <li>in the worst case, converts from source to {@link Iterable} to {@link Collection} to target</li>
 * </ul>
 */
@StandardOperator
@DependsOn({ GetArrayElementType.class, CollectionToArray.class, IterableToList.class, DefaultToListConverter.class })
public class DefaultToArrayConverter extends Converter.WithDynamicTarget<Object> {

    @Override
    public boolean perform(TherianContext context, final Convert<?, ?> convert) {

        @SuppressWarnings("rawtypes")
        final Position.ReadWrite<Iterable> iterable = Positions.readWrite(Iterable.class);
        if (convert.getSourcePosition().getValue() instanceof Iterable<?>) {
            iterable.setValue((Iterable<?>) convert.getSourcePosition().getValue());
        } else if (!context.evalSuccess(Convert.to(iterable, convert.getSourcePosition()))) {
            return false;
        }
        if (iterable.getValue() instanceof Collection<?> == false) {
            iterable.setValue(context.eval(Convert.to(Collection.class, iterable)));
        }
        return context.forwardTo(Convert.to(convert.getTargetPosition(), iterable));
    }

    @Override
    public boolean supports(TherianContext context, Convert<?, ?> convert) {
        if (!(super.supports(context, convert) && TypeUtils.isArrayType(convert.getTargetPosition().getType()))) {
            return false;
        }
        if (!(convert.getSourcePosition().getValue() instanceof Iterable<?> || context.supports(Convert.to(
            Iterable.class, convert.getSourcePosition())))) {
            return false;
        }
        final GetElementType<?> getTargetElementType = GetElementType.of(convert.getTargetPosition());
        if (!context.supports(getTargetElementType)) {
            return false;
        }
        final Type targetElementType = context.eval(getTargetElementType);
        final Type sourceElementType;
        final GetElementType<?> getSourceElementType = GetElementType.of(convert.getSourcePosition());
        if (context.supports(getSourceElementType)) {
            sourceElementType = context.eval(getSourceElementType);
        } else {
            // if element type not available, assume we're wrapping an arbitrary object as a singleton
            sourceElementType = convert.getSourcePosition().getType();
        }
        return TypeUtils.isAssignable(sourceElementType, targetElementType);
    }

}
