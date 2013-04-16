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
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import therian.Operator;
import therian.TherianContext;
import therian.operation.Convert;
import therian.util.Types;

/**
 * {@link Convert} {@link Operator} superclass.
 * 
 * @param <SOURCE>
 * @param <TARGET>
 */
public abstract class Converter<SOURCE, TARGET> implements Operator<Convert<? extends SOURCE, ? super TARGET>> {
    private static final TypeVariable<?>[] TYPE_PARAMS = Converter.class.getTypeParameters();

    /**
     * {@link Logger} instance.
     */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    public boolean supports(TherianContext context, Convert<? extends SOURCE, ? super TARGET> convert) {
        final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(getClass(), Converter.class);
        return TypeUtils.isInstance(convert.getSourcePosition().getValue(), Types.unrollVariables(typeArguments, TYPE_PARAMS[0]))
            && TypeUtils.isAssignable(Types.unrollVariables(typeArguments, TYPE_PARAMS[1]), convert.getTargetPosition().getType());
    }
}
