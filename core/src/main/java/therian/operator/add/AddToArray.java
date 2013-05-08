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

import java.lang.reflect.Array;
import java.lang.reflect.Type;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Add;
import therian.operation.GetElementType;
import therian.operation.Size;
import therian.operator.convert.NOPConverter;
import therian.operator.getelementtype.GetArrayElementType;
import therian.operator.size.DefaultSizeOperator;
import therian.position.Position;
import therian.position.Position.Writable;
import therian.util.Positions;

/**
 * Overwrites an array at a {@link Writable} {@link Position}s with an enlarged array.
 */
@StandardOperator
@DependsOn({ DefaultSizeOperator.class, GetArrayElementType.class, NOPConverter.class })
public class AddToArray implements Operator<Add<?, ?>> {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean perform(TherianContext context, Add<?, ?> add) {
        final Type targetElementType = context.eval(GetElementType.of(add.getTargetPosition()));
        final Position.Writable target = (Position.Writable) add.getTargetPosition();
        final int origSize = context.eval(Size.of(add.getTargetPosition()));
        final Object enlarged = Array.newInstance(TypeUtils.getRawType(targetElementType, null), origSize + 1);
        if (origSize > 0) {
            System.arraycopy(add.getTargetPosition().getValue(), 0, enlarged, 0, origSize);
        }
        Array.set(enlarged, origSize, add.getSourcePosition().getValue());
        target.setValue(enlarged);
        add.setResult(true);
        return true;
    }

    @Override
    public boolean supports(TherianContext context, Add<?, ?> add) {
        if (!(TypeUtils.isArrayType(add.getTargetPosition().getType()) && Positions.isWritable(add.getTargetPosition()))) {
            return false;
        }
        final Type targetElementType = context.eval(GetElementType.of(add.getTargetPosition()));
        return TypeUtils.isInstance(add.getSourcePosition().getValue(), targetElementType);
    }

}
