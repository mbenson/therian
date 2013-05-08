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
import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import therian.Operator;
import therian.TherianContext;
import therian.Operator.DependsOn;
import therian.operation.Copy;
import therian.operation.ImmutableCheck;
import therian.operator.immutablecheck.DefaultImmutableChecker;
import therian.util.Types;

/**
 * {@link Copy} {@link Operator} superclass.
 *
 * Note the assignability constraints:
 * <ul>
 * <li>SOURCE assignable from source type</li>
 * <li>TARGET assignable from target type</li>
 * </ul>
 *
 * For example, a {@link Copier} of {@link CharSequence} to {@link Number} (not that this is a realistic operation, but
 * play along for the sake of discussion) could handle a copy of {@link String} to {@link Integer} as well as
 * {@link StringBuilder} to {@link BigDecimal}. Thus it is best that your {@link Copier} implementations parameterize
 * both SOURCE and TARGET as widely as possible.
 *
 * @param <SOURCE>
 * @param <TARGET>
 */
@DependsOn(DefaultImmutableChecker.class)
public abstract class Copier<SOURCE, TARGET> implements Operator<Copy<? extends SOURCE, ? extends TARGET>> {
    /**
     * {@link Logger} instance.
     */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final Type sourceBound;
    private final Type targetBound;

    {
        final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(getClass(), Copier.class);
        sourceBound = Types.unrollVariables(typeArguments, Copier.class.getTypeParameters()[0]);
        targetBound = Types.unrollVariables(typeArguments, Copier.class.getTypeParameters()[1]);
    }

    // override default parameter name
    @Override
    public abstract boolean perform(TherianContext context, Copy<? extends SOURCE, ? extends TARGET> copy);

    /**
     * By default, rejects immutable target positions, and ensures that type parameters are compatible.
     *
     * @param copy operation
     *
     * @see ImmutableCheck
     */
    public boolean supports(TherianContext context, Copy<? extends SOURCE, ? extends TARGET> copy) {
        if (context.eval(ImmutableCheck.of(copy.getTargetPosition())).booleanValue() && isRejectImmutable()) {
            return false;
        }
        return TypeUtils.isInstance(copy.getSourcePosition().getValue(), sourceBound)
            && TypeUtils.isAssignable(copy.getTargetPosition().getType(), targetBound);
    }

    /**
     * Learn whether to reject immutable targets.
     *
     * @return true
     */
    protected boolean isRejectImmutable() {
        return true;
    }
}
