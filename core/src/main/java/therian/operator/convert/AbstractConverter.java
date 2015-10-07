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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operation;
import therian.Operator;
import therian.operation.Convert;
import therian.operator.FromSourceToTarget;
import therian.util.Types;

/**
 * Abstract {@link Convert} {@link Operator} superclass.
 */
public abstract class AbstractConverter<T extends Operation<?>> extends FromSourceToTarget<T> {

    /**
     * Learn whether a {@link Convert} operation's source value is (already) an instance of its target type,
     *
     * @param convert
     * @return boolean
     */
    protected final boolean isNoop(Convert<?, ?> convert) {
        Type sourceType = convert.getSourcePosition().getType();
        if (ParameterizedType.class.isInstance(sourceType) && convert.getSourcePosition().getValue() != null) {
            sourceType =
                Types.narrowestParameterizedType(convert.getSourcePosition().getValue().getClass(),
                    (ParameterizedType) sourceType);
        }
        if (TypeUtils.isAssignable(sourceType, convert.getTargetPosition().getType())) {
            if (ParameterizedType.class.isInstance(convert.getTargetPosition().getType())) {
                // make sure all type params of target position are accounted for by source before declaring it a noop:
                final Class<?> rawTargetType = TypeUtils.getRawType(convert.getTargetPosition().getType(), null);
                final Map<TypeVariable<?>, Type> typeMappings = TypeUtils.getTypeArguments(sourceType, rawTargetType);

                for (TypeVariable<?> v : rawTargetType.getTypeParameters()) {
                    if (typeMappings.get(v) == null) {
                        return false;
                    }
                    if (typeMappings.get(v) instanceof TypeVariable<?>) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Should this {@link AbstractConverter} detect (and reject) conversions can, by virtue of source value/target type
     * assignability, can be considered noops?
     *
     * @return true
     */
    protected boolean isRejectNoop() {
        return true;
    }
}
