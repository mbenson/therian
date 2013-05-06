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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Convert;
import therian.operation.GetElementType;
import therian.operator.getelementtype.GetArrayElementType;
import therian.operator.getelementtype.GetIterableElementType;

/**
 * Converts arrays, wraps other objects in {@link Collections#singletonList(Object)}.
 */
@SuppressWarnings("rawtypes")
@StandardOperator
@DependsOn({ GetIterableElementType.class, GetArrayElementType.class })
public class DefaultToListConverter extends Converter<Object, List> {

    @Override
    public boolean perform(TherianContext context, Convert<? extends Object, ? super List> convert) {
        final List<?> list;
        if (TypeUtils.isArrayType(convert.getSourcePosition().getType())) {
            final Object[] array;
            final Object source = convert.getSourcePosition().getValue();
            if (source instanceof Object[]) {
                array = (Object[]) source;
            } else {
                final Class<?> primitiveType =
                    (Class<?>) TypeUtils.getArrayComponentType(convert.getSourcePosition().getType());
                final int len = Array.getLength(source);
                array = (Object[]) Array.newInstance(ClassUtils.primitiveToWrapper(primitiveType), len);
                for (int i = 0; i < len; i++) {
                    array[i] = Array.get(source, i);
                }
            }
            list = Arrays.asList(array);
        } else {
            list = Collections.singletonList(convert.getSourcePosition().getValue());
        }
        convert.getTargetPosition().setValue(list);
        return true;
    }

    @Override
    public boolean supports(TherianContext context, Convert<?, ? super List> convert) {
        if (!super.supports(context, convert) || convert.getSourcePosition().getValue() == null) {
            return false;
        }

        final GetElementType<?> getTargetElementType = GetElementType.of(convert.getTargetPosition());
        if (!context.supports(getTargetElementType)) {
            return false;
        }
        final Type targetElementType = context.eval(getTargetElementType);
        final GetElementType<?> getSourceElementType = GetElementType.of(convert.getSourcePosition());
        final Type sourceElementType;
        if (context.supports(getSourceElementType)) {
            sourceElementType = context.eval(getSourceElementType);
        } else {
            sourceElementType = convert.getSourcePosition().getType();
        }
        return TypeUtils.isAssignable(sourceElementType, targetElementType);
    }

}
