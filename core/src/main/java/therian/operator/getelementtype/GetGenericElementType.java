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
package therian.operator.getelementtype;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.reflect.Typed;

import therian.Operator;
import therian.TherianContext;
import therian.operation.GetElementType;
import therian.operator.OptimisticOperatorBase;

/**
 * Abstract base class for {@link GetElementType} {@link Operator} implementations that resolve a {@link TypeVariable}
 * against a {@link Typed}.
 */
public abstract class GetGenericElementType<T> extends OptimisticOperatorBase<GetElementType<T>> {
    private final TypeVariable<Class<T>> typeVariable;

    protected GetGenericElementType(TypeVariable<Class<T>> typeVariable) {
        this.typeVariable = Objects.requireNonNull(typeVariable);
    }

    @Override
    public final boolean perform(TherianContext context, GetElementType<T> op) {
        final Type result = ObjectUtils.defaultIfNull(TypeUtils.unrollVariables(
            TypeUtils.getTypeArguments(op.getTypedItem().getType(), typeVariable.getGenericDeclaration()),
            typeVariable), Object.class);
        op.setResult(result);
        return true;
    }
}
