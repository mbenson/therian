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

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator.DependsOn;
import therian.TherianContext;
import therian.buildweaver.StandardOperator;
import therian.operation.Convert;
import therian.util.Positions;

/**
 * Convert an enum value to any number, by way of {@link Enum#ordinal()}. Because multiple target types are served (by
 * delegation), the destination parameter is unspecified at the class level.
 */
@StandardOperator
@DependsOn({ ELCoercionConverter.class, NOPConverter.class })
public class EnumToNumberConverter extends Converter.WithDynamicTarget<Enum<?>> {

    @Override
    public boolean perform(TherianContext context, Convert<? extends Enum<?>, ?> operation) {
        return context.evalSuccess(Convert.to(operation.getTargetPosition(),
            Positions.readOnly(operation.getSourcePosition().getValue().ordinal())));
    }

    @Override
    public boolean supports(TherianContext context, Convert<? extends Enum<?>, ?> operation) {
        if (!TypeUtils.isAssignable(operation.getSourcePosition().getType(), Enum.class)) {
            return false;
        }
        final Type targetType = operation.getTargetPosition().getType();
        if (!(targetType instanceof Class)) {
            return false;
        }
        final Class<?> targetClass = (Class<?>) targetType;
        return Number.class.isAssignableFrom(targetClass) && ClassUtils.wrapperToPrimitive(targetClass) != null;
    }
}
