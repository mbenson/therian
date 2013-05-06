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

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Convert;
import therian.operation.GetElementType;
import therian.operator.getelementtype.GetArrayElementType;
import therian.position.Position;

@SuppressWarnings("rawtypes")
@StandardOperator
@DependsOn(GetArrayElementType.class)
public class CollectionToArray extends Converter.WithDynamicTarget<Collection> {

    @SuppressWarnings("unchecked")
    @Override
    public boolean perform(TherianContext context, Convert<? extends Collection, ?> convert) {
        final Type targetElementType = context.eval(GetElementType.of(convert.getTargetPosition()));
        final int size = convert.getSourcePosition().getValue().size();

        final Object result = Array.newInstance(TypeUtils.getRawType(targetElementType, null), size);

        final Object[] toFill;
        final boolean primitiveTargetElementType =
            targetElementType instanceof Class<?> && ((Class<?>) targetElementType).isPrimitive();

        if (primitiveTargetElementType) {
            toFill = (Object[]) Array.newInstance(ClassUtils.primitiveToWrapper((Class<?>) targetElementType), size);
        } else {
            toFill = (Object[]) result;
        }
        convert.getSourcePosition().getValue().toArray(toFill);
        if (primitiveTargetElementType) {
            for (int i = 0; i < size; i++) {
                Array.set(result, i, toFill[i]);
            }
        }
        ((Position.Writable<Object>) convert.getTargetPosition()).setValue(result);
        return true;
    }

    @Override
    public boolean supports(TherianContext context, Convert<? extends Collection, ?> convert) {
        if (!super.supports(context, convert)) {
            return false;
        }
        if (convert.getSourcePosition().getValue() == null) {
            return false;
        }
        if (!TypeUtils.isArrayType(convert.getTargetPosition().getType())) {
            return false;
        }
        final GetElementType<?> getTargetElementType = GetElementType.of(convert.getTargetPosition());
        if (!context.supports(getTargetElementType)) {
            return false;
        }
        final Type targetElementType = context.eval(getTargetElementType);

        for (Object element : convert.getSourcePosition().getValue()) {
            if (!TypeUtils.isInstance(element, targetElementType)) {
                return false;
            }
        }
        return true;
    }
}
