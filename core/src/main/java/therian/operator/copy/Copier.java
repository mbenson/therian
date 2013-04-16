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
package therian.operator.copy;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import therian.Operator;
import therian.TherianContext;
import therian.operation.Copy;
import therian.operation.ImmutableCheck;
import therian.util.Types;

/**
 * {@link Copy} {@link Operator} superclass.
 * 
 * @param <SOURCE>
 * @param <TARGET>
 */
public abstract class Copier<SOURCE, TARGET> implements Operator<Copy<? extends SOURCE, ? extends TARGET>> {
    private static final TypeVariable<?>[] TYPE_PARAMS = Copier.class.getTypeParameters();

    /**
     * {@link Logger} instance.
     */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * By default, rejects immutable target positions, and ensures that type parameters are compatible.
     * 
     * @param copy operation
     * 
     * @see ImmutableCheck
     */
    public boolean supports(TherianContext context, Copy<? extends SOURCE, ? extends TARGET> copy) {
        // cannot copy to immutable types
        if (context.eval(ImmutableCheck.of(copy.getTargetPosition())).booleanValue()) {
            return false;
        }
        final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(getClass(), Copier.class);
        return TypeUtils.isInstance(copy.getSourcePosition().getValue(),
            Types.unrollVariables(typeArguments, TYPE_PARAMS[0]))
            && TypeUtils.isAssignable(copy.getTargetPosition().getType(),
                Types.unrollVariables(typeArguments, TYPE_PARAMS[1]));
    }
}
