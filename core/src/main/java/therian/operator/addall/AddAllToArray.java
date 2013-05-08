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
package therian.operator.addall;

import java.lang.reflect.Array;
import java.lang.reflect.Type;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.AddAll;
import therian.operation.Convert;
import therian.operation.GetElementType;
import therian.operation.Size;
import therian.operator.convert.DefaultToArrayConverter;
import therian.operator.convert.NOPConverter;
import therian.operator.size.DefaultSizeOperator;
import therian.position.Position;
import therian.position.Position.Writable;
import therian.util.Positions;

/**
 * For an array at a {@link Writable} {@link Position}s, converts source to array, then overwrites with an enlarged
 * array.
 */
@StandardOperator
@DependsOn({ DefaultToArrayConverter.class, DefaultSizeOperator.class, NOPConverter.class })
public class AddAllToArray implements Operator<AddAll<?, ?>> {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean perform(TherianContext context, AddAll<?, ?> addAll) {
        Object array =
            context.eval(Convert.to(Positions.<Object> readWrite(addAll.getTargetPosition().getType()),
                addAll.getSourcePosition()));

        final Position.Writable target = (Position.Writable) addAll.getTargetPosition();
        final int origSize = context.eval(Size.of(addAll.getTargetPosition()));
        if (origSize > 0) {
            final int toAdd = context.eval(Size.of(Positions.readOnly(array)));
            final Type targetElementType = context.eval(GetElementType.of(addAll.getTargetPosition()));
            final Object enlarged = Array.newInstance(TypeUtils.getRawType(targetElementType, null), origSize + toAdd);
            System.arraycopy(addAll.getTargetPosition().getValue(), 0, enlarged, 0, origSize);
            System.arraycopy(array, 0, enlarged, origSize, toAdd);
            array = enlarged;
        }
        target.setValue(array);
        addAll.setResult(true);
        return true;
    }

    @Override
    public boolean supports(TherianContext context, AddAll<?, ?> addAll) {
        if (!(TypeUtils.isArrayType(addAll.getTargetPosition().getType()) && Positions.isWritable(addAll
            .getTargetPosition()))) {
            return false;
        }
        final GetElementType<?> getSourceElementType = GetElementType.of(addAll.getSourcePosition());
        final GetElementType<?> getTargetElementType = GetElementType.of(addAll.getTargetPosition());
        if (!(context.supports(getSourceElementType) && context.supports(getTargetElementType))) {
            return false;
        }
        final Type sourceElementType = context.eval(getSourceElementType);
        final Type targetElementType = context.eval(getTargetElementType);
        return TypeUtils.isAssignable(sourceElementType, targetElementType)
            && context.supports(Convert.to(Positions.readWrite(addAll.getTargetPosition().getType()),
                addAll.getSourcePosition()));
    }

}
