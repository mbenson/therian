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
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Convert;
import therian.operation.GetElementType;
import therian.operator.getelementtype.GetEnumerationElementType;
import therian.position.Position;
import therian.util.Positions;

/**
 * Attempts to convert to {@link Iterator} and thence to {@link Enumeration}.
 */
@SuppressWarnings("rawtypes")
@StandardOperator
@DependsOn({ DefaultToIteratorConverter.class, GetEnumerationElementType.class, IteratorToEnumeration.class })
public class DefaultToEnumerationConverter extends Converter<Object, Enumeration> {

    @Override
    public boolean perform(TherianContext context, Convert<? extends Object, ? super Enumeration> convert) {
        final Type targetElementType = context.eval(GetElementType.of(convert.getTargetPosition()));
        Type[] typeArguments = { targetElementType };
        final Position.ReadWrite<Iterator<?>> iterator =
            Positions.readWrite(TypeUtils.parameterize(Iterator.class, typeArguments));
        return context.evalSuccess(Convert.to(iterator, convert.getSourcePosition()))
            && context.evalSuccess(Convert.to(convert.getTargetPosition(), iterator));
    }

    @Override
    public boolean supports(TherianContext context, Convert<?, ? super Enumeration> convert) {
        if (!super.supports(context, convert)) {
            return false;
        }
        final Convert<?, Iterator> toIterator = Convert.to(Iterator.class, convert.getSourcePosition());

        if (!context.supports(toIterator)) {
            return false;
        }
        final GetElementType<?> getTargetElementType = GetElementType.of(convert.getTargetPosition());
        if (!context.supports(getTargetElementType)) {
            return false;
        }
        final GetElementType<?> getSourceElementType = GetElementType.of(convert.getSourcePosition());
        if (!context.supports(getSourceElementType)) {
            return false;
        }
        final Type targetElementType = context.eval(getTargetElementType);
        final Type sourceElementType = context.eval(getSourceElementType);

        return TypeUtils.isAssignable(sourceElementType, targetElementType);
    }

}
