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
package net.morph.operator;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import net.morph.MorphContext;
import net.morph.Operator;
import net.morph.operation.Copy;
import net.morph.operation.ImmutableCheck;

import org.apache.commons.lang3.reflect.TypeUtils;

/**
 * {@link Copy} {@link Operator} superclass.
 * 
 * @param <SOURCE>
 * @param <DEST>
 */
public abstract class Copier<SOURCE, DEST> implements Operator<Copy<? extends SOURCE, ? extends DEST>> {
    private static final TypeVariable<?>[] TYPE_PARAMS = Copier.class.getTypeParameters();

    public boolean supports(Copy<? extends SOURCE, ? extends DEST> copy) {
        // cannot copy to immutable types
        if (MorphContext.getRequiredInstance().eval(ImmutableCheck.of(copy.getTargetPosition().getValue()))
            .booleanValue()) {
            return false;
        }
        final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(getClass(), Copier.class);
        return TypeUtils.isInstance(copy.getSourcePosition().getValue(), typeArguments.get(TYPE_PARAMS[0]))
            && TypeUtils.isAssignable(copy.getTargetPosition().getType(), typeArguments.get(TYPE_PARAMS[1]));
    }
}
